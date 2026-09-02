package br.com.carloslonghi.eletrolonghi.support;

import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.RefreshToken;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static Brand brand(Long id) {
        return Brand.builder().id(id).name("Marca " + id).build();
    }

    public static Accessory accessory(Long id) {
        return Accessory.builder().id(id).name("Acessorio " + id).build();
    }

    public static Device device(Long id) {
        return Device.builder()
                .id(id)
                .model("Modelo " + id)
                .serialNumber("SERIAL-" + id)
                .brand(brand(1L))
                .accessories(List.of(accessory(1L)))
                .build();
    }

    public static Customer customer(Long id) {
        return Customer.builder()
                .id(id)
                .name("Cliente " + id)
                .phone("1199999000" + id)
                .email("cliente" + id + "@mail.com")
                .build();
    }

    public static RepairOrder repairOrder(Long id) {
        return RepairOrder.builder()
                .id(id)
                .description("Descricao " + id)
                .status(RepairOrderStatus.AWAITING_EVALUATION)
                .customer(customer(1L))
                .device(device(1L))
                .build();
    }

    public static Payment payment(Long id) {
        return Payment.builder()
                .id(id)
                .amount(new BigDecimal("100.00"))
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.PENDING)
                .installments(1)
                .repairOrder(repairOrder(1L))
                .build();
    }

    public static User user(Long id) {
        return User.builder()
                .id(id)
                .name("Usuario " + id)
                .email("user" + id + "@mail.com")
                .password("senha")
                .role(Role.USER)
                .build();
    }

    public static RefreshToken refreshToken(String token, User user, Instant expiryDate, boolean revoked) {
        return RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .revoked(revoked)
                .build();
    }
}
