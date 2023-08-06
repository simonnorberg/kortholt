package org.puredata.core

internal class PdBase {
    external fun initialize()
    external fun addToSearchPath(dir: String)
    external fun openFile(patch: String, dir: String): Long
    external fun closeFile(ptr: Long)

    external fun sendBang(receiver: String): Int
    external fun sendFloat(receiver: String, x: Float): Int

    fun sendList(receiver: String, vararg args: Any) {
        if (startMessage(args.size) == 0) {
            var success = true
            for (arg in args) {
                when (arg) {
                    is Int -> addFloat(arg.toFloat())
                    is Float -> addFloat(arg)
                    is Double -> addFloat(arg.toFloat())
                    is String -> addSymbol(arg)
                    else -> {
                        success = false
                        break
                    }
                }
            }
            if (success) {
                finishList(receiver)
            }
        }
    }

    private external fun startMessage(length: Int): Int
    private external fun addFloat(x: Float)
    private external fun addSymbol(symbol: String)
    private external fun finishList(receiver: String): Int
}
