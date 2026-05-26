package com.andr1anka.readyforit.service;

import com.andr1anka.readyforit.dto.BookingRequestDTO;
import com.andr1anka.readyforit.dto.BookingResponseDTO;

public interface BookingService {

    /**
     * Записати студента на урок:
     * перевіряє вільність слота, відсутність перетинів у розкладі студента
     * та достатність балансу; списує кошти (escrow на застосунку) і створює урок.
     */
    BookingResponseDTO book(String email, BookingRequestDTO request);
}
