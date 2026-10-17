package com.thiagoxavier.taskmanager.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService("segredo-de-teste-com-pelo-menos-32-caracteres!!", 5);

    @Test
    void geraEValidaToken() {
        String token = jwtService.generateToken("ana@email.com");

        assertThat(jwtService.extractValidSubject(token)).contains("ana@email.com");
    }

    @Test
    void rejeitaTokenAdulterado() {
        String token = jwtService.generateToken("ana@email.com");
        String adulterado = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.extractValidSubject(adulterado)).isEmpty();
    }

    @Test
    void rejeitaTokenAssinadoComOutraChave() {
        JwtService outro = new JwtService("outro-segredo-totalmente-diferente-com-32-chars", 5);
        String token = outro.generateToken("ana@email.com");

        assertThat(jwtService.extractValidSubject(token)).isEmpty();
    }

    @Test
    void exigeSegredoMinimoDe32Caracteres() {
        assertThatThrownBy(() -> new JwtService("curto", 5))
                .isInstanceOf(IllegalStateException.class);
    }
}
