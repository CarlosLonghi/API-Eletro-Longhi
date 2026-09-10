package br.com.carloslonghi.eletrolonghi.repository;

import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.repository.specification.DeviceSpecification;
import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DeviceRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private AccessoryRepository accessoryRepository;

    @BeforeEach
    void clean() {
        deviceRepository.deleteAll();
        accessoryRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    void shouldFindBySerialAndBrandIdAndSpecification() {
        Brand brand1 = brandRepository.save(Brand.builder().name("Marca-A").build());
        Brand brand2 = brandRepository.save(Brand.builder().name("Marca-B").build());
        Accessory accessory = accessoryRepository.save(Accessory.builder().name("Cabo").build());

        Device d1 = deviceRepository.save(Device.builder()
                .model("Notebook Pro")
                .serialNumber("SER-001")
                .brand(brand1)
                .accessories(List.of(accessory))
                .build());

        deviceRepository.save(Device.builder()
                .model("TV Smart")
                .serialNumber("SER-002")
                .brand(brand2)
                .accessories(List.of(accessory))
                .build());

        assertThat(deviceRepository.findBySerialNumber("SER-001")).contains(d1);
        assertThat(deviceRepository.findDevicesByBrandId(brand1.getId())).hasSize(1);
        assertThat(deviceRepository.findAll(DeviceSpecification.withFilters("note", brand1.getId()))).hasSize(1);
        assertThat(deviceRepository.findAll(DeviceSpecification.withFilters("inexistente", null))).isEmpty();
    }

    @Test
    void shouldPageAndSortByNestedBrandProperty() {
        Brand brandB = brandRepository.save(Brand.builder().name("Marca-B").build());
        Brand brandA = brandRepository.save(Brand.builder().name("Marca-A").build());

        deviceRepository.save(Device.builder()
                .model("Notebook Pro")
                .serialNumber("SER-001")
                .brand(brandB)
                .accessories(List.of())
                .build());
        deviceRepository.save(Device.builder()
                .model("TV Smart")
                .serialNumber("SER-002")
                .brand(brandA)
                .accessories(List.of())
                .build());

        Page<Device> page = deviceRepository.findAll(
                DeviceSpecification.withFilters(null, null),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "brand.id"))
        );

        assertThat(page.getContent())
                .extracting(device -> device.getBrand().getId())
                .containsExactly(brandB.getId(), brandA.getId());
    }
}

