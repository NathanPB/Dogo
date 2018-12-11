package io.github.dogo.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/*
Copyright 2019 Nathan Bombana

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
 * Utility static methods about web.
 *
 * @author NathanPB
 * @since 3.1.0
 */
class WebUtils {
    companion object {

        /**
         * Sends a GET request to [url]. It can also be specified GET [args] and http [headers].
         *
         * @param[url] The URL to fetch
         * @param[args] GET arguments (optional)
         * @param[headers] HTTP Headers (optional)
         *
         * @return the body of the page.
         * @throws java.io.IOException
         */
        fun get(url: String, args: Array<Pair<String, String>> = emptyArray(), headers: Array<Pair<String, String>> = emptyArray()): String {
            var url = url
            if (args.isNotEmpty()) {
                url += args.map {
                    Pair(URLEncoder.encode(it.first, "UTF-8"), URLEncoder.encode(it.second, "UTF-8"))
                }.joinToString(separator = "&", prefix = "&")
            }
            val response = StringBuilder()
            (URL(url).openConnection() as HttpURLConnection).let {
                it.requestMethod = "GET"
                headers.forEach { h ->
                    it.setRequestProperty(h.first, h.second)
                }
                it.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")


                it.inputStream.also {
                    BufferedReader(InputStreamReader(it)).also {
                        do {
                            val line = it.readLine()?.let { response.append("$it\n") }
                        } while (line != null)
                    }.close()
                }.close()
            }
            return response.toString()
        }
    }
}