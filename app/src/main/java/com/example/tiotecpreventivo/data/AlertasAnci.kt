package com.example.tiotecpreventivo.data

import com.google.gson.annotations.SerializedName

data class PaginaAlertasAnci(
    @SerializedName("items")
    val items: List<AlertaAnci>,
    @SerializedName("count")
    val count: Int
)

data class AlertaAnci(
    @SerializedName("title")
    val titulo: String?,
    @SerializedName("code")
    val codigo: String?,
    @SerializedName("category")
    val categoria: String?,
    @SerializedName("incident_type")
    val tipoIncidente: String?,
    @SerializedName("date")
    val fecha: String?,
    @SerializedName("general_description")
    val descripcionGeneral: String?,
    @SerializedName("mitigation")
    val mitigacion: String?,
    @SerializedName("related_links")
    val enlacesRelacionados: List<EnlaceAnci>?,
    @SerializedName("affected_entities")
    val entidadesAfectadas: List<String>?,
    @SerializedName("specific_description")
    val descripcionEspecifica: String?
)

data class EnlaceAnci(
    @SerializedName("name")
    val nombre: String?,
    @SerializedName("url")
    val url: String?
)
