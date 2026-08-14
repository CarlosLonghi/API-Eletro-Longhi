package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.request.RepairOrderRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.RepairOrderStatusUpdateRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.RepairOrderResponse;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.mapper.RepairOrderMapper;
import br.com.carloslonghi.eletrolonghi.service.RepairOrderService;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairOrderControllerTest {

    @Mock
    private RepairOrderService repairOrderService;

    @Mock
    private RepairOrderMapper repairOrderMapper;

    @InjectMocks
    private RepairOrderController repairOrderController;

    @Test
    void shouldReturnPagedRepairOrders() {
        RepairOrder order = TestFixtures.repairOrder(1L);
        RepairOrderResponse response = RepairOrderResponse.builder().id(1L).status(RepairOrderStatus.AWAITING_EVALUATION).build();
        when(repairOrderService.findAll(any(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(order)));
        when(repairOrderMapper.toResponse(order)).thenReturn(response);

        var result = repairOrderController.getAllRepairOrders(null, null, null, null, null, 0, 10, "id", "asc");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).containsExactly(response);
    }

    @Test
    void shouldCreateRepairOrder() {
        RepairOrderRequest request = new RepairOrderRequest("desc", RepairOrderStatus.AWAITING_EVALUATION, 1L, 1L);
        RepairOrder entity = TestFixtures.repairOrder(1L);
        RepairOrderResponse response = RepairOrderResponse.builder().id(1L).status(RepairOrderStatus.AWAITING_EVALUATION).build();

        when(repairOrderMapper.toEntity(request)).thenReturn(entity);
        when(repairOrderService.save(entity)).thenReturn(entity);
        when(repairOrderMapper.toResponse(entity)).thenReturn(response);

        var result = repairOrderController.createRepairOrder(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldUpdateStatusWhenFound() {
        RepairOrder entity = TestFixtures.repairOrder(1L);
        RepairOrderResponse response = RepairOrderResponse.builder().id(1L).status(RepairOrderStatus.IN_REPAIR).build();

        when(repairOrderService.updateStatus(1L, RepairOrderStatus.IN_REPAIR)).thenReturn(Optional.of(entity));
        when(repairOrderMapper.toResponse(entity)).thenReturn(response);

        var result = repairOrderController.updateRepairOrderStatus(1L, new RepairOrderStatusUpdateRequest(RepairOrderStatus.IN_REPAIR));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldGetRepairOrderByIdWhenFound() {
        RepairOrder entity = TestFixtures.repairOrder(1L);
        RepairOrderResponse response = RepairOrderResponse.builder().id(1L).status(RepairOrderStatus.AWAITING_EVALUATION).build();
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(entity));
        when(repairOrderMapper.toResponse(entity)).thenReturn(response);

        var result = repairOrderController.getRepairOrderById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdMissing() {
        when(repairOrderService.findById(1L)).thenReturn(Optional.empty());

        var result = repairOrderController.getRepairOrderById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateRepairOrderWhenFound() {
        RepairOrderRequest request = new RepairOrderRequest("desc", RepairOrderStatus.IN_REPAIR, 1L, 1L);
        RepairOrder entity = TestFixtures.repairOrder(1L);
        RepairOrderResponse response = RepairOrderResponse.builder().id(1L).status(RepairOrderStatus.IN_REPAIR).build();
        when(repairOrderMapper.toEntity(request)).thenReturn(entity);
        when(repairOrderService.update(1L, entity)).thenReturn(Optional.of(entity));
        when(repairOrderMapper.toResponse(entity)).thenReturn(response);

        var result = repairOrderController.updateRepairOrder(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDeleteRepairOrderWhenFound() {
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(TestFixtures.repairOrder(1L)));

        var result = repairOrderController.deleteRepairOrderById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturnNotFoundOnDeleteRepairOrderWhenMissing() {
        when(repairOrderService.findById(1L)).thenReturn(Optional.empty());

        var result = repairOrderController.deleteRepairOrderById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}


