package io.loli.maikaze.kancolle

import com.alibaba.fastjson.JSON
import io.loli.maikaze.utils.HttpClientUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

/**
 * Created by uzuma on 2017/8/20.
 */
public class LoginContext {

    String $1_dmm_token, $1_token, $1_html

    String $2_token, $2_login_id, $2_password, $2_html

    String $3_html

    String $4_url_from_html, $4_html, $4_owner, $4_st

    String $5_html, $5_world, $5_world_ip

    String $6_api_token, $6_api_starttime, $6_flash_url

    String user_agent
    String username;
    String password;
    public KancolleProperties properties;
    public HttpClientUtil httpClientUtil;

    def reset(username, password) {
        $1_dmm_token = null;
        $1_token = null;
        $1_html = null
        $2_token = null
        $2_login_id = null
        $2_password = null
        $2_html = null
        $3_html = null
        $4_url_from_html = null
        $4_html = null
        $4_owner = null
        $4_st = null
        $5_html = null
        $5_world = null
        $5_world_ip = null
        $6_api_token = null
        $6_api_starttime = null

        $6_flash_url = null
        this.username = username
        this.password = password
    }


    def startLogin() {
        loginToken()
        ajaxGetToken()
        login()
        getUserIdFromIframeUrl()
        getServerIpWithUserId()
        getGameTokenWithServerIp()
    }


    def loginToken() {
        def loginPageResult = httpClientUtil.get(properties.loginUrl, null, properties.loginTokenHeaders);
        $1_html = loginPageResult
        def dmmToken = (loginPageResult =~ /"DMM_TOKEN", "(.+)(?=")/)[0][1]
        def token = (loginPageResult =~ /"token": "(.+)(?=")/)[0][1]
        $1_dmm_token = dmmToken
        $1_token = token
    }


    def ajaxGetToken() {
        def dmmToken = $1_dmm_token
        def token = $1_token
        properties.idKeysHeaders['DMM_TOKEN'] = dmmToken
        def idKeysResult = httpClientUtil.post(properties.ajaxGetTokenUrl, ['token': token], properties.idKeysHeaders);
        $2_html = idKeysResult
        def resMap = JSON.parseObject(idKeysResult, Map)
        $2_login_id = resMap['login_id']
        $2_token = resMap['token']
        $2_password = resMap['password']
    }

    def login() {
        def token = $2_token
        def loginIdToken = $2_login_id
        def passwordToken = $2_password
        def params = [
                'login_id'     : username,
                'password'     : password,
                'token'        : token,
                (loginIdToken) : username,
                (passwordToken): password
        ]

        def res = httpClientUtil.post(properties.getAuthUrl(), params, properties.loginAuthHeaders)
        $3_html = res
    }

    def getUserIdFromIframeUrl() {
        def gameResult = httpClientUtil.get(properties.getGameUrl(), null, properties.simpleHeaders)
        $4_html = gameResult
        String frameUrl = (gameResult =~ /(?<=")(.*osapi.*owner=.*)(?=")/)[0][0]
        frameUrl = frameUrl.substring(0, frameUrl.lastIndexOf("#"));
        $4_url_from_html = frameUrl
        def params = UriComponentsBuilder.fromHttpUrl(frameUrl).build().getQueryParams();
        $4_owner = params['owner'][0]
        $4_st = new URLDecoder().decode(params['st'][0],'UTF-8')
    }

    def getServerIpWithUserId() {
        def headers = properties.simpleHeaders.clone();
        headers['Referer'] = $4_url_from_html
        def serverIpResult = httpClientUtil.get(String.format(properties.getWorldUrl, $4_owner, System.currentTimeMillis()), null, headers)
        $5_html = serverIpResult
        def object = JSON.parseObject(serverIpResult.substring(7), Map)
        $5_world = object['api_data']['api_world_id'];
        $5_world_ip = properties.serverList[Integer.parseInt($5_world) - 1]
    }


    def getGameTokenWithServerIp() {
        String url = String.format(properties.getFlashUrl, $5_world_ip, $4_owner, System.currentTimeMillis())
        def params = [
                'url'         : url,
                'httpMethod'  : 'GET',
                'authz'       : 'signed',
                'st'          : $4_st,
                'contentType' : 'JSON',
                'numEntries'  : '3',
                'getSummaries': 'false',
                'signOwner'   : 'true',
                'signViewer'  : 'true',
                'gadget'      : 'http://203.104.209.7/gadget.xml',
                'container'   : 'dmm'
        ]

        def makeRequestResult = httpClientUtil.get(properties.getMakeRequestUrl(), params, properties.simpleHeaders)
        String availableString = makeRequestResult.substring(27)
        def obj = JSON.parseObject(availableString, Map)
        availableString = obj[url]['body'].substring(7);
        obj = JSON.parseObject(availableString, Map)
        $6_api_token = obj['api_token'];
        $6_api_starttime = obj['api_starttime']

        $6_flash_url = String.format(properties.mainFlashUrl, $5_world_ip, $6_api_token, $6_api_starttime)

        $6_flash_url
    }

}
