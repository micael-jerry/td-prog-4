package com.spring.boot.service;

import com.spring.boot.model.Cin;
import com.spring.boot.model.Email;
import com.spring.boot.model.Employee;
import com.spring.boot.model.Image;
import com.spring.boot.model.Phone;
import com.spring.boot.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class EmployeeService {
    private EmployeeRepository employeeRepository;
    private ImageService imageService;
    private CinService cinService;
    private EmailService emailService;
    private PhoneService phoneService;
    private AddressService addressService;

    public List<Employee> findAllWithCriteria(
            String function, String lastname, String firstname, String sex, String startDate, String departureDate, String orderBy, String direction) {
        if (orderBy.length() > 1) {
            if (this.isValidDate(startDate) && this.isValidDate(departureDate)) {
                return employeeRepository
                        .findAllByCriteriaBetweenStartAndDepartureWithSort(function, lastname, firstname, sex, startDate, departureDate, orderBy, direction);
            } else if (this.isValidDate(startDate) && !this.isValidDate(departureDate)) {
                return employeeRepository
                        .findAllByCriteriaAfterStartWithSort(function, lastname, firstname, sex, startDate, orderBy, direction);
            } else if (!this.isValidDate(startDate) && this.isValidDate(departureDate)) {
                return employeeRepository
                        .findAllByCriteriaBeforeDepartureWithSort(function, lastname, firstname, sex, departureDate, orderBy, direction);
            }
            return employeeRepository
                    .findAllByCriteriaWithSort(function, lastname, firstname, sex, orderBy, direction);
        } else {
            if (this.isValidDate(startDate) && this.isValidDate(departureDate)) {
                return employeeRepository
                        .findAllByCriteriaBetweenStartAndDeparture(function, lastname, firstname, sex, startDate, departureDate);
            } else if (this.isValidDate(startDate) && !this.isValidDate(departureDate)) {
                return employeeRepository
                        .findAllByCriteriaAfterStart(function, lastname, firstname, sex, startDate);
            } else if (!this.isValidDate(startDate) && this.isValidDate(departureDate)) {
                return employeeRepository
                        .findAllByCriteriaBeforeDeparture(function, lastname, firstname, sex, departureDate);
            }
            return employeeRepository
                    .findAllByCriteria(function, lastname, firstname, sex);
        }
    }

    @Transactional
    public Employee save(Employee employee, MultipartFile image) throws IOException {
//        Save image
        Image imageSaved = imageService.save(image);
        employee.setId_image(imageSaved.getId());
//        Save cin
        Cin cinSaved = cinService.save(employee.getCin());
        employee.setCin(cinSaved);
//        Save Email
        Email personal = emailService.save(employee.getPersonalEmail());
        employee.setPersonalEmail(personal);
        Email professional = emailService.save(employee.getProfessionalEmail());
        employee.setProfessionalEmail(professional);

        List<Phone> phones = employee.getPhones();
        Employee employeeSaved = employeeRepository.save(employee);
        employeeSaved.setPhones(null);
        phoneService.saveAll(phones, employeeSaved);
        return employeeSaved;
    }

    public Employee update(Employee employee, MultipartFile image) throws IOException {
//        Image update
        Image imageUpdated = imageService.update(employee.getId_image(), image);
        employee.setId_image(imageUpdated.getId());
//        Cin update
        cinService.update(employee.getCin());
//        Email update
        emailService.update(employee.getPersonalEmail());
        emailService.update(employee.getProfessionalEmail());
//        Address update
        addressService.update(employee.getAddress());

        return employeeRepository.save(employee);
    }

    public Optional<Employee> findById(Integer id) {
        return employeeRepository.findById(id);
    }

    public String exportUrlParams(String function, String lastname, String firstname, String sex, String orderBy, String direction) {
        return "?firstname_filter=" + firstname.replaceAll(" ", "+") +
                "&lastname_filter=" + lastname.replaceAll(" ", "+") +
                "&function_filter=" + function.replaceAll(" ", "+") +
                "&sex_filter=" + sex +
                "&order_by=" + orderBy +
                "&order_direction=" + direction;
    }

    private boolean isValidDate(String date) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            simpleDateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            log.error("PARSE DATE ERROR: " + date);
            return false;
        }
    }
}
