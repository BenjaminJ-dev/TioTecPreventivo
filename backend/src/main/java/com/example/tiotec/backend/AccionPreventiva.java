package com.example.tiotec.backend;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AccionPreventiva(
        @NotNull @PositiveOrZero Integer id,
        @NotNull Boolean completada
) {}
