<#-- @ftlvariable name="" type="com.yammer.schedulizer.freemarker.ExtAppConfigView" -->

<#if extApp == "yammer">
    <script type="text/javascript" data-app-id="${extAppClientId}" src="https://c64.assets-yammer.com/assets/platform_js_sdk.js"></script>
<#elseif extApp == "facebook">
    <script id="facebook-jssdk" src="//connect.facebook.net/en_US/sdk.js"></script>
    <script>
        FB.init({
            appId      : '${extAppClientId}',
            xfbml      : true,
            version    : 'v2.3'
        });
    </script>
</#if>


<script>
    var EXT_APP_TYPES_CONSTANT = {
        <#list extAppTypes as extAppType>
            ${extAppType}: "${extAppType}",
        </#list>
    };
    var EXT_APP_CONSTANT = EXT_APP_TYPES_CONSTANT.${extApp};

</script>


