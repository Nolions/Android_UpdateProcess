package tw.nolions.updateprocess

class Model {

    class LoginReq(val username: String, val password: String)
    class LoginResp(val uid: String, val username: String, val type: String, val token: String)

    data class CoverResp(val cover: String)
}

data class RespDataModel<T>(val isSuccess: Boolean?, val code: Int = 0, val message: String? = null, val data: T? = null, val error: Error? = null) {
    data class Error(val code: String, val message: String, val errors: HashMap<String, ArrayList<String>>)
}