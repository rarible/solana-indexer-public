package com.rarible.protocol.solana.nft.listener.util

import org.springframework.boot.json.GsonJsonParser
import org.springframework.web.client.RestTemplate
import java.util.*

//https://api.solanart.io/get_nft?collection=degenape&page=3&limit=20&order=price-ASC&min=0&max=99999&search=&listed=true&fits=all&bid=all

private val urlTemplate = "https://api.solanart.io/get_nft?collection=%s&page=%s&limit=100&order=price-ASC&min=0&max=99999&search=&fits=all&bid=all"

// for utilitary purposes only
class SolanaArtCollectionClient

fun main() {
    //val collection = "degenape"
    //val collection = "degeneratetrashpandas" //TODO only 14800 items returned
    val collection = "degenerate_ape_kindergarten"

    val rest = RestTemplate()

    var page = 0

    val tokens = TreeSet<String>()
    do {
        val url = urlTemplate.format(collection, page)
        val response = rest.getForObject(url, String::class.java)!!
        val parsed = GsonJsonParser().parseMap(response)
        val items = parsed["items"] as List<Any?>
        items.forEach {
            val item = it as Map<String, Any?>
            tokens.add(item["token_add"] as String)
        }
        println("${items.size} downloaded for page = $page")
        page++
        Thread.sleep(1000)
    } while (items.isNotEmpty())

    tokens.forEach {
        println(it)
    }

}

