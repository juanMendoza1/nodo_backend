package com.nodo.inv.jwt;

import com.nodo.inv.entity.TerminalDispositivo;
import com.nodo.inv.repository.TerminalDispositivoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TerminalAuthenticationFilter extends OncePerRequestFilter {

    private final TerminalDispositivoRepository terminalRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String terminalUuid = request.getHeader("X-Terminal-UUID");

        // 🔥 Si NO es una tablet (ej. React), lo dejamos pasar al filtro JWT
        if (terminalUuid == null || terminalUuid.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔥 Si SÍ es una tablet, la validamos
        try {
            TerminalDispositivo terminal = terminalRepo.findByUuidHardware(terminalUuid)
                    .orElseThrow(() -> new RuntimeException("Terminal no vinculada"));

            if (terminal.getBloqueado() != null && terminal.getBloqueado()) {
                throw new RuntimeException("Terminal bloqueada");
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "TABLET_" + terminal.getUuidHardware(), null, List.of(new SimpleGrantedAuthority("ROLE_OPERATIVO"))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Terminal inválida o bloqueada");
            return;
        }

        filterChain.doFilter(request, response);
    }
}