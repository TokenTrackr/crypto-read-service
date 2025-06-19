package com.tokentrackr.crypto_read_service.controller;

import com.tokentrackr.crypto_read_service.model.request.GetAllCryptoRequest;
import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;
import com.tokentrackr.crypto_read_service.service.interfaces.GetAllCryptoService;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CryptoController.class)
class CryptoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAllCryptoService getAllCryptoService;

    @MockitoBean
    private MeterRegistry meterRegistry;

    @Test
    void getAllCrypto_returnsEmptyList() throws Exception {
        // Mock the DistributionSummary
        DistributionSummary mockSummary = mock(DistributionSummary.class);
        when(meterRegistry.summary("crypto_assets_returned")).thenReturn(mockSummary);

        GetAllCryptoResponse response = GetAllCryptoResponse.builder()
                .crypto(Collections.emptyList())
                .build();

        when(getAllCryptoService.getAllCrypto(any(GetAllCryptoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(get("/crypto")
                        .param("page", "1")
                        .param("size", "10")
                        .param("marketCapRank", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"crypto\":[]}"));
    }
}