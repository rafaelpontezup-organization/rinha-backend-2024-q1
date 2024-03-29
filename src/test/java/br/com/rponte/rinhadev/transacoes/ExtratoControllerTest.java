package br.com.rponte.rinhadev.transacoes;

import base.SpringBootIntegrationTest;
import br.com.rponte.rinhadev.transacoes.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExtratoControllerTest extends SpringBootIntegrationTest {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private TransacaoRepository transacaoRepository;

    private static Cliente ZAN;
    private static Cliente RAFAEL;

    @BeforeEach
    public void setUp() {
        transacaoRepository.deleteAll();
        clienteRepository.deleteAll();
        clienteRepository.saveAll(List.of(
                ZAN = new Cliente("Zan", 1960L, 6000L),
                RAFAEL = new Cliente("Rafael Ponte", -299L, 500L)
        ));
    }

    @Test
    @DisplayName("deve imprimir extrato do cliente com as ultimas 10 transacoes")
    public void t1() throws Exception {
        // cenario
        transacaoRepository.saveAll(List.of(
                // zan = $1960
                new Transacao(1000L, TipoDeTransacao.CREDITO, "c1" , ZAN),
                new Transacao(400L , TipoDeTransacao.CREDITO, "c2" , ZAN),
                new Transacao(800L , TipoDeTransacao.CREDITO, "c3" , ZAN),
                new Transacao(300L , TipoDeTransacao.DEBITO , "d4" , ZAN),
                new Transacao(400L , TipoDeTransacao.CREDITO, "c5" , ZAN),
                new Transacao(400L , TipoDeTransacao.DEBITO , "d6" , ZAN),
                new Transacao(250L , TipoDeTransacao.DEBITO , "d7" , ZAN),
                new Transacao(300L , TipoDeTransacao.CREDITO, "c8" , ZAN),
                new Transacao(120L , TipoDeTransacao.DEBITO , "d9" , ZAN),
                new Transacao(1000L, TipoDeTransacao.DEBITO , "d10", ZAN),
                new Transacao(240L , TipoDeTransacao.CREDITO, "c11", ZAN),
                new Transacao(890L , TipoDeTransacao.CREDITO, "c12", ZAN),
                // rafael = $-299
                new Transacao(299L , TipoDeTransacao.DEBITO, "d13" , RAFAEL)
        ));

        // ação (+validação)
        mockMvc.perform(get("/clientes/{id}/extrato", ZAN.getId())
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").exists())
                    .andExpect(jsonPath("$.saldo.total").value(1960L))
                    .andExpect(jsonPath("$.saldo.limite").value(6000))
                    .andExpect(jsonPath("$.saldo.data_extrato").isNotEmpty())
                .andExpect(jsonPath("$.ultimas_transacoes", hasSize(10)))
                    .andExpect(jsonPath("$.ultimas_transacoes[0].descricao").value("c12"))
                    .andExpect(jsonPath("$.ultimas_transacoes[1].descricao").value("c11"))
                    .andExpect(jsonPath("$.ultimas_transacoes[2].descricao").value("d10"))
                    .andExpect(jsonPath("$.ultimas_transacoes[3].descricao").value("d9" ))
                    .andExpect(jsonPath("$.ultimas_transacoes[4].descricao").value("c8" ))
                    .andExpect(jsonPath("$.ultimas_transacoes[5].descricao").value("d7" ))
                    .andExpect(jsonPath("$.ultimas_transacoes[6].descricao").value("d6" ))
                    .andExpect(jsonPath("$.ultimas_transacoes[7].descricao").value("c5" ))
                    .andExpect(jsonPath("$.ultimas_transacoes[8].descricao").value("d4" ))
                    .andExpect(jsonPath("$.ultimas_transacoes[9].descricao").value("c3" ))
        ;
    }

    @Test
    @DisplayName("deve imprimir extrato do cliente quando cliente possui menos de 10 transações")
    public void t2() throws Exception {
        // cenario
        transacaoRepository.saveAll(List.of(
                new Transacao(299L , TipoDeTransacao.DEBITO, "d1" , RAFAEL)
        ));

        // ação (+validação)
        mockMvc.perform(get("/clientes/{id}/extrato", RAFAEL.getId())
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").exists())
                    .andExpect(jsonPath("$.saldo.total").value(-299))
                    .andExpect(jsonPath("$.saldo.limite").value(500L))
                    .andExpect(jsonPath("$.saldo.data_extrato").isNotEmpty())
                .andExpect(jsonPath("$.ultimas_transacoes", hasSize(1)))
                    .andExpect(jsonPath("$.ultimas_transacoes[0].descricao").value("d1"))
        ;
    }

    @Test
    @DisplayName("deve imprimir extrato do cliente quando cliente não possui transações")
    public void t3() throws Exception {
        // ação (+validação)
        mockMvc.perform(get("/clientes/{id}/extrato", RAFAEL.getId())
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").exists())
                    .andExpect(jsonPath("$.saldo.total").value(-299L))
                    .andExpect(jsonPath("$.saldo.limite").value(500))
                    .andExpect(jsonPath("$.saldo.data_extrato").isNotEmpty())
                .andExpect(jsonPath("$.ultimas_transacoes").isEmpty())
        ;
    }

    @Test
    @DisplayName("não deve imprimir extrato quando cliente não encontrado")
    public void t4() throws Exception {
        // cenario
        Long clienteInexistenteId = -9999L;

        // ação (+validação)
        mockMvc.perform(get("/clientes/{id}/extrato", clienteInexistenteId)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("cliente não encontrado")))
        ;
    }
}