package ru.udya.autoconfigure.rdatamanager;

import ru.udya.rdatamanager.RdmConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({RdmConfiguration.class})
public class RdmAutoConfiguration {
}

