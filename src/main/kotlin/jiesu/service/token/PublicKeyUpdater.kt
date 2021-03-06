package jiesu.service.token

import jiesu.service.model.PublicKeyHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.web.client.RestTemplate
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.RSAPublicKeySpec

class PublicKeyUpdater(val discoveryClient: DiscoveryClient, val publicKeyHolder: PublicKeyHolder) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(PublicKeyUpdater::class.java)
    }

    fun refreshPublicKey() {
        val fileswims: List<ServiceInstance> = discoveryClient.getInstances("fileswim")
        if (fileswims.isEmpty()) {
            log.info("Couldn't get public key from Fileswim, Fileswim instance not found")
            return
        }

        val pubKey: Array<String>? = RestTemplate().getForObject("${fileswims[0].uri}/tokenPubKey", Array<String>::class.java)
        if (pubKey == null) {
            log.info("Couldn't get public key from Fileswim, failed calling Fileswim endpoint.")
        } else {
            val spec = RSAPublicKeySpec(BigInteger(pubKey[0]), BigInteger(pubKey[1]))
            val publicKey = KeyFactory.getInstance("RSA").generatePublic(spec)
            synchronized(publicKeyHolder) {
                publicKeyHolder.publicKey = publicKey
            }
        }
    }
}
