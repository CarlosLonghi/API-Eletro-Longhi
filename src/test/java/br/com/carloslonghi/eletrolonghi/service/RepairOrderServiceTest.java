package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.exception.DeviceAlreadyInRepairException;
import br.com.carloslonghi.eletrolonghi.exception.InvalidRepairOrderStatusTransitionException;
import br.com.carloslonghi.eletrolonghi.exception.ReferencedEntityNotFoundException;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceTest {

    @Mock
    private RepairOrderRepository repairOrderRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private RepairOrderService repairOrderService;

    @Test
    void shouldFindWithFilters() {
        Page<RepairOrder> page = new PageImpl<>(List.of(TestFixtures.repairOrder(1L)));
        when(repairOrderRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<RepairOrder> result = repairOrderService.findAll(
                RepairOrderStatus.AWAITING_EVALUATION,
                1L,
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldSaveRepairOrderWhenDeviceIsAvailable() {
        RepairOrder order = TestFixtures.repairOrder(1L);
        when(customerService.findById(1L)).thenReturn(Optional.of(order.getCustomer()));
        when(deviceService.findById(1L)).thenReturn(Optional.of(order.getDevice()));
        when(repairOrderRepository.existsByDeviceIdAndStatusNot(1L, RepairOrderStatus.DEVICE_COLLECTED)).thenReturn(false);
        when(repairOrderRepository.save(order)).thenReturn(order);

        RepairOrder saved = repairOrderService.save(order);

        assertThat(saved).isEqualTo(order);
    }

    @Test
    void shouldFailSavingWhenDeviceAlreadyInRepair() {
        RepairOrder order = TestFixtures.repairOrder(1L);
        when(customerService.findById(1L)).thenReturn(Optional.of(order.getCustomer()));
        when(deviceService.findById(1L)).thenReturn(Optional.of(order.getDevice()));
        when(repairOrderRepository.existsByDeviceIdAndStatusNot(1L, RepairOrderStatus.DEVICE_COLLECTED)).thenReturn(true);

        assertThatThrownBy(() -> repairOrderService.save(order))
                .isInstanceOf(DeviceAlreadyInRepairException.class);
    }

    @Test
    void shouldUpdateStatusWhenFound() {
        RepairOrder order = TestFixtures.repairOrder(1L);
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(repairOrderRepository.save(order)).thenReturn(order);

        Optional<RepairOrder> updated = repairOrderService.updateStatus(1L, RepairOrderStatus.IN_EVALUATION);

        assertThat(updated).isPresent();
        assertThat(order.getStatus()).isEqualTo(RepairOrderStatus.IN_EVALUATION);
        verify(repairOrderRepository).save(order);
    }

    @Test
    void shouldRejectStatusTransitionThatSkipsSteps() {
        RepairOrder order = TestFixtures.repairOrder(1L);
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> repairOrderService.updateStatus(1L, RepairOrderStatus.IN_REPAIR))
                .isInstanceOf(InvalidRepairOrderStatusTransitionException.class);
    }

    @Test
    void shouldFindByIdAndDelete() {
        RepairOrder order = TestFixtures.repairOrder(1L);
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThat(repairOrderService.findById(1L)).contains(order);
        repairOrderService.deleteById(1L);
        verify(repairOrderRepository).deleteById(1L);
    }

    @Test
    void shouldUpdateRepairOrderWhenFound() {
        RepairOrder existing = TestFixtures.repairOrder(1L);
        RepairOrder incoming = TestFixtures.repairOrder(2L);
        incoming.setStatus(RepairOrderStatus.IN_EVALUATION);
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerService.findById(1L)).thenReturn(Optional.of(existing.getCustomer()));
        when(deviceService.findById(1L)).thenReturn(Optional.of(existing.getDevice()));
        when(repairOrderRepository.save(existing)).thenReturn(existing);

        Optional<RepairOrder> updated = repairOrderService.update(1L, incoming);

        assertThat(updated).isPresent();
        assertThat(existing.getStatus()).isEqualTo(RepairOrderStatus.IN_EVALUATION);
    }

    @Test
    void shouldRejectRepairOrderUpdateWithStatusSkip() {
        RepairOrder existing = TestFixtures.repairOrder(1L);
        RepairOrder incoming = TestFixtures.repairOrder(2L);
        incoming.setStatus(RepairOrderStatus.IN_REPAIR);
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> repairOrderService.update(1L, incoming))
                .isInstanceOf(InvalidRepairOrderStatusTransitionException.class);
    }

    @Test
    void shouldThrowWhenCustomerMissingOnSave() {
        RepairOrder order = TestFixtures.repairOrder(1L);
        when(customerService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repairOrderService.save(order))
                .isInstanceOf(ReferencedEntityNotFoundException.class);
    }

    @Test
    void shouldThrowWhenDeviceMissingOnSave() {
        RepairOrder order = TestFixtures.repairOrder(1L);
        when(customerService.findById(1L)).thenReturn(Optional.of(order.getCustomer()));
        when(deviceService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repairOrderService.save(order))
                .isInstanceOf(ReferencedEntityNotFoundException.class);
    }

    @Test
    void shouldReturnEmptyWhenUpdateOrStatusMissing() {
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(repairOrderService.update(1L, TestFixtures.repairOrder(1L))).isEmpty();
        assertThat(repairOrderService.updateStatus(1L, RepairOrderStatus.IN_REPAIR)).isEmpty();
    }

    @Test
    void shouldAdvanceToPaymentReceivedWhenOrderIsRepairCompleted() {
        RepairOrder order = TestFixtures.repairOrder(1L);
        order.setStatus(RepairOrderStatus.REPAIR_COMPLETED);
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(repairOrderRepository.save(order)).thenReturn(order);

        repairOrderService.markPaymentReceived(1L);

        assertThat(order.getStatus()).isEqualTo(RepairOrderStatus.PAYMENT_RECEIVED);
        verify(repairOrderRepository).save(order);
    }

    @Test
    void shouldNotAdvanceWhenOrderIsNotRepairCompleted() {
        RepairOrder order = TestFixtures.repairOrder(1L);
        order.setStatus(RepairOrderStatus.IN_REPAIR);
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        repairOrderService.markPaymentReceived(1L);

        assertThat(order.getStatus()).isEqualTo(RepairOrderStatus.IN_REPAIR);
        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }

    @Test
    void shouldIgnoreMarkPaymentReceivedWhenOrderMissing() {
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.empty());

        repairOrderService.markPaymentReceived(1L);

        verify(repairOrderRepository, never()).save(any(RepairOrder.class));
    }
}


