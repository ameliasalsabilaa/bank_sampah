package com.amelia.asesmengenap.banksampah.Domain

data class OrdersModel(
    var id: String = "",
    var idUser: String = "",
    var namabarang: List<String> = ArrayList(),
    var picUrl: MutableList<String> = ArrayList(),
    var jumlahsetiapitem: List<Int> = ArrayList(),
    var hargabarang: ArrayList<Int> = ArrayList(),
    var subtotalharga: Int = 0,
    var pajakongkir: Int = 0,
    var subtotalkeseluruhan: Int = 0,
    var perizinan: Boolean? = null,
    var username: String = ""
)