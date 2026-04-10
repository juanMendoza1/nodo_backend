package com.nodo.inv.service;

import com.nodo.inv.dto.LiquidacionDTO;
import com.nodo.inv.entity.*;
import com.nodo.inv.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiquidacionService {

    private final LiquidacionRepository liquidacionRepository;
    private final ConceptoLiquidacionRepository conceptoLiquidacionRepository;
    private final ConceptoRepository conceptoRepository;
    private final EmpresaRepository empresaRepository;
    private final ProgramaRepository programaRepository;

    /**
     * 1. CREAR PLANTILLA (SUPERADMIN):
     * Crea la base de la liquidación que verán todas las empresas de un programa.
     */
    @Transactional
    public Liquidacion crearPlantillaGlobal(LiquidacionDTO dto) {
        Programa programa = programaRepository.findById(dto.programaId())
                .orElseThrow(() -> new RuntimeException("Programa no encontrado"));

        Liquidacion liq = new Liquidacion();
        liq.setCodigo(dto.codigo().toUpperCase());
        liq.setNombre(dto.nombre());
        liq.setPrograma(programa);
        liq.setEsGlobal(true);

        return liquidacionRepository.save(liq);
    }

    /**
     * 2. CONFIGURAR RECETA (ADMIN o SUPERADMIN):
     * Asocia conceptos reales a una plantilla para una empresa específica.
     */
    @Transactional
    public void configurarConceptos(Long empresaId, LiquidacionDTO dto) {
        Liquidacion plantilla = liquidacionRepository.findByCodigo(dto.codigo())
                .orElseThrow(() -> new RuntimeException("Plantilla base no encontrada"));
        
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        Programa programa = programaRepository.findById(dto.programaId())
                .orElseThrow(() -> new RuntimeException("Programa no encontrado"));

        // A. Limpiamos la configuración anterior para esta empresa/plantilla
        List<ConceptoLiquidacion> antiguos = conceptoLiquidacionRepository.obtenerRecetaDeLiquidacion(
                dto.codigo(), empresaId, dto.programaId());
        conceptoLiquidacionRepository.deleteAll(antiguos);

        // B. Insertamos la nueva "Receta"
        List<ConceptoLiquidacion> nuevaReceta = dto.conceptosRelacionados().stream().map(det -> {
            Concepto concepto = conceptoRepository.findById(det.conceptoId())
                    .orElseThrow(() -> new RuntimeException("Concepto ID " + det.conceptoId() + " no existe"));

            ConceptoLiquidacion cl = new ConceptoLiquidacion();
            cl.setLiquidacion(plantilla);
            cl.setConcepto(concepto);
            cl.setEmpresa(empresa);
            cl.setPrograma(programa);
            cl.setOrdenCalculo(det.ordenCalculo());
            return cl;
        }).collect(Collectors.toList());

        conceptoLiquidacionRepository.saveAll(nuevaReceta);
    }
    
    /**
     * 3. OBTENER RECETA CONFIGURADA:
     * Devuelve cómo quedó armada la receta para una empresa (Útil para mostrar en el Frontend al editar).
     */
    @Transactional(readOnly = true)
    public List<ConceptoLiquidacion> obtenerConfiguracionActual(String codigoLiquidacion, Long empresaId, Long programaId) {
        return conceptoLiquidacionRepository.obtenerRecetaDeLiquidacion(codigoLiquidacion, empresaId, programaId);
    }
}