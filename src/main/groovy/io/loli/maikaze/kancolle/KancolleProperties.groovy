package io.loli.maikaze.kancolle;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by uzuma on 2017/8/20.
 */
@ConfigurationProperties(prefix = "kancolle")
class KancolleProperties {
    List<String> serverList;
    def loginUrl;
    def ajaxGetTokenUrl;
    def authUrl;
    def gameUrl;
    def makeRequestUrl;
    def getWorldUrl;
    def getFlashUrl;
    def mainFlashUrl;
    def loginTokenHeaders = ['User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36']

    def idKeysHeaders = [
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36',
            'Origin':'https://www.dmm.com',
            'DMM_TOKEN':'',
            'X-Requested-With':'XMLHttpRequest'
    ]

    def loginAuthHeaders = [
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36',
            'Origin':'https://www.dmm.com',
    ]
    def simpleHeaders = [
             'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36',
            'Origin':'https://www.dmm.com',

    ]



    def proxy;
    def proxyIp;
    def proxyPort;
    def proxyType;
}
