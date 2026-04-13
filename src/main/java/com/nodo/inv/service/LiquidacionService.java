package com.nodo.inv.service;

import com.nodo.inv.core.entity.Concepto;
import com.nodo.inv.core.entity.Empresa;
import com.nodo.inv.core.entity.Programa;
import com.nodo.inv.core.entity.TipoDocumento;
import com.nodo.inv.core.repository.ConceptoRepository;
import com.nodo.inv.core.repository.EmpresaRepository;
import com.nodo.inv.core.repository.ProgramaRepository;
import com.nodo.inv.core.repository.TipoDocumentoRepository;
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
    // 🔥 NUEVO: Repositorio inyectado para buscar el Tipo de Documento
    private final TipoDocumentoRepository tipoDocumentoRepository;

    /**
     * 1. CREAR PLANTILLA (SUPERADMIN):
     * Crea la base de la liquidación que verán todas las empresas de un programa.
     */
    @Transactional
    public Liquidacion crearPlantillaGlobal(LiquidacionDTO dto) {
        
        // 🔥 VALIDAR Y BUSCAR EL TIPO DE DOCUMENTO (Obligatorio)
        TipoDocumento tipoDoc = tipoDocumentoRepository.findById(dto.tipoDocumentoId())
                .orElseThrow(() -> new RuntimeException("Debe seleccionar un Tipo de Documento válido"));

        Programa programa = null;
        
        // LÓGICA TRANSVERSAL: Si el programaId NO es nulo ni 0, lo buscamos.
        // De lo contrario, queda como null, lo que significa que es un esquema Global.
        if (dto.programaId() != null && dto.programaId() != 0) {
            programa = programaRepository.findById(dto.programaId())
                    .orElseThrow(() -> new RuntimeException("Programa no encontrado"));
        }

        Liquidacion liq = new Liquidacion();
        liq.setCodigo(dto.codigo().toUpperCase());
        liq.setNombre(dto.nombre());
        liq.setPrograma(programa); // Será null si es transversal
        liq.setEsGlobal(true);
        liq.setTipoDocumentoGenerado(tipoDoc); // 🔥 Sellamos la relación entre Liquidación y Documento

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

        Programa programa = null;
        
        // LÓGICA TRANSVERSAL: Mismo tratamiento para la configuración
        if (dto.programaId() != null && dto.programaId() != 0) {
            programa = programaRepository.findById(dto.programaId())
                    .orElseThrow(() -> new RuntimeException("Programa no encontrado"));
        }

        // A. Limpiamos la configuración anterior para esta empresa/plantilla
        // Usamos el programaId original del DTO para la consulta (0 si es transversal)
        Long progIdBuscador = (dto.programaId() == null) ? 0L : dto.programaId();
        
        List<ConceptoLiquidacion> antiguos = conceptoLiquidacionRepository.obtenerRecetaDeLiquidacion(
                dto.codigo(), empresaId, progIdBuscador);
        conceptoLiquidacionRepository.deleteAll(antiguos);

        // B. Insertamos la nueva "Receta"
        final Programa finalPrograma = programa; // Requisito de Java para usarlo dentro de la expresión lambda
        
        List<ConceptoLiquidacion> nuevaReceta = dto.conceptosRelacionados().stream().map(det -> {
            Concepto concepto = conceptoRepository.findById(det.conceptoId())
                    .orElseThrow(() -> new RuntimeException("Concepto ID " + det.conceptoId() + " no existe"));

            ConceptoLiquidacion cl = new ConceptoLiquidacion();
            cl.setLiquidacion(plantilla);
            cl.setConcepto(concepto);
            cl.setEmpresa(empresa);
            cl.setPrograma(finalPrograma); // Será null si es un concepto transversal
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
        // En tu ConceptoLiquidacionRepository ya configuramos que si programaId es 0, busque donde programa IS NULL
        return conceptoLiquidacionRepository.obtenerRecetaDeLiquidacion(codigoLiquidacion, empresaId, programaId);
    }
}