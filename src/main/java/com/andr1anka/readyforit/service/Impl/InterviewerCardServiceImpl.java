package com.andr1anka.readyforit.service.Impl;

import com.andr1anka.readyforit.dto.InterviewerCardDTO;
import com.andr1anka.readyforit.dto.InterviewerFilterDTO;
import com.andr1anka.readyforit.dto.PagedResponseDTO;
import com.andr1anka.readyforit.mapper.InterviewerCardMapper;
import com.andr1anka.readyforit.repository.InformationAboutLessonRepository;
import com.andr1anka.readyforit.repository.InterviewerRequestRepository;
import com.andr1anka.readyforit.service.InterviewerCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewerCardServiceImpl implements InterviewerCardService {

    private static final int DEFAULT_PAGE_SIZE = 6;

    private final InformationAboutLessonRepository lessonRepository;
    private final InterviewerCardMapper mapper;
    private final InterviewerRequestRepository requestRepository;

    @Autowired
    public InterviewerCardServiceImpl(InformationAboutLessonRepository lessonRepository,
                                      InterviewerCardMapper mapper,
                                      InterviewerRequestRepository requestRepository) {
        this.lessonRepository = lessonRepository;
        this.mapper = mapper;
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewerCardDTO> getAllCards() {
        // кеш років досвіду по userId, щоб уникнути N+1 запитів
        Map<Long, Integer> experienceByUser = new HashMap<>();

        return lessonRepository.findAll().stream()
                .map(lesson -> {
                    InterviewerCardDTO dto = mapper.toDTO(lesson);
                    if (dto == null) return null;
                    Long userId = (lesson.getInterviewer() == null || lesson.getInterviewer().getUser() == null)
                            ? null : lesson.getInterviewer().getUser().getId();
                    if (userId != null) {
                        Integer years = experienceByUser.computeIfAbsent(userId, uid ->
                                requestRepository.findTopByUserOrderByCreatedAtDesc(lesson.getInterviewer().getUser())
                                        .map(r -> r.getYearsOfExperience())
                                        .orElse(null));
                        dto.setExperienceYears(years);
                    }
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<InterviewerCardDTO> getCards(InterviewerFilterDTO filter) {
        if (filter == null) filter = new InterviewerFilterDTO();

        // 1) усі картки
        List<InterviewerCardDTO> all = getAllCards();

        // 2) фільтрація
        List<String> wantedTags = normalizeTags(filter.getTags());
        Integer minPrice = filter.getMinPrice();
        Integer maxPrice = filter.getMaxPrice();
        String search = filter.getSearch() == null ? "" : filter.getSearch().trim().toLowerCase();

        List<InterviewerCardDTO> filtered = all.stream()
                .filter(c -> minPrice == null || c.getPrice() >= minPrice)
                .filter(c -> maxPrice == null || c.getPrice() <= maxPrice)
                .filter(c -> wantedTags.isEmpty() || matchesAnyTag(c, wantedTags))
                .filter(c -> search.isEmpty() || matchesSearch(c, search))
                .collect(Collectors.toList());

        // 3) сортування
        Comparator<InterviewerCardDTO> comparator = switch (filter.getSort() == null ? "" : filter.getSort()) {
            case "price_asc" -> Comparator.comparingInt(InterviewerCardDTO::getPrice);
            case "price_desc" -> Comparator.comparingInt(InterviewerCardDTO::getPrice).reversed();
            case "rank_desc" -> Comparator.comparing(
                    c -> c.getRank() == null ? -1.0 : c.getRank(),
                    Comparator.reverseOrder());
            default -> Comparator.comparing(InterviewerCardDTO::getId); // стабільний порядок
        };
        filtered.sort(comparator);

        // 4) пагінація
        int page = filter.getPage() == null || filter.getPage() < 0 ? 0 : filter.getPage();
        int size = filter.getSize() == null || filter.getSize() <= 0 ? DEFAULT_PAGE_SIZE : filter.getSize();

        long total = filtered.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<InterviewerCardDTO> pageContent = filtered.subList(fromIndex, toIndex);

        return PagedResponseDTO.<InterviewerCardDTO>builder()
                .content(new ArrayList<>(pageContent))
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        Set<String> tags = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (InterviewerCardDTO c : getAllCards()) {
            if (c.getTags() != null) tags.addAll(c.getTags());
        }
        return new ArrayList<>(tags);
    }

    // ---- допоміжні ----
    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) return Collections.emptyList();
        return tags.stream()
                .filter(Objects::nonNull)
                .map(t -> t.trim().toLowerCase())
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
    }

    private boolean matchesAnyTag(InterviewerCardDTO c, List<String> wantedLower) {
        if (c.getTags() == null) return false;
        for (String t : c.getTags()) {
            if (wantedLower.contains(t.trim().toLowerCase())) return true;
        }
        return false;
    }

    private boolean matchesSearch(InterviewerCardDTO c, String searchLower) {
        String haystack = ((c.getName() == null ? "" : c.getName()) + " "
                + (c.getLastName() == null ? "" : c.getLastName()) + " "
                + (c.getTitle() == null ? "" : c.getTitle()) + " "
                + (c.getShortDescription() == null ? "" : c.getShortDescription())).toLowerCase();
        return haystack.contains(searchLower);
    }
}
