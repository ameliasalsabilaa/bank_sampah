package com.amelia.asesmengenap.banksampah.Domain

class ModelPengguna (
    val idUser : String = "",
    val username : String = "",
    val alamat : String = "",
    val password: String = "" // Tambahkan field password
) {
    constructor():this("", "","", "")
}
