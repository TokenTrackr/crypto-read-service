//package com.tokentrackr.crypto_read_service.integration;
//
//import com.tokentrackr.crypto_read_service.model.response.GetAllCryptoResponse;
//import com.tokentrackr.crypto_read_service.service.interfaces.GetAllCryptoService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Collections;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class CryptoControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private GetAllCryptoService getAllCryptoService;
//
//    @Test
//    void contextLoadsAndEndpointUp() throws Exception {
//        when(getAllCryptoService.getAllCrypto(org.mockito.ArgumentMatchers.any()))
//                .thenReturn(GetAllCryptoResponse.builder().crypto(Collections.emptyList()).build());
//
//        mockMvc.perform(get("/crypto").accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//    }
//}
