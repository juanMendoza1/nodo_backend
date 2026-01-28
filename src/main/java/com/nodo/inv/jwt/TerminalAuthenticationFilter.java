package com.nodo.inv.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nodo.inv.repository.TerminalDispositivoRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TerminalAuthenticationFilter extends OncePerRequestFilter {

    private final TerminalDispositivoRepository terminalRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uuid = request.getHeader("X-Terminal-UUID");

        if (uuid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Verificamos si la terminal existe y está activa en nuestra DB
            terminalRepository.findByUuidHardware(uuid).ifPresent(terminal -> {
                if (!terminal.getBloqueado()) {
                    // Creamos una autoridad de OPERATIVO para esta terminal
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_OPERATIVO"));
                    
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            uuid, null, authorities);
                    
                    // Seteamos la autenticación en el contexto de Spring
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            });
        }

        filterChain.doFilter(request, response);
    }
}
