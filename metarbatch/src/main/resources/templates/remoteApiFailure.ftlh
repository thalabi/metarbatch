<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
</head>
<body>

  <#if (loadingFromExternalApiException.messageList?size gt 0)>
     <span style="font-style: underline;">${httpRequestTypeEnum.serviceDescription} failed with the following exception(s):</span>
	  <#list loadingFromExternalApiException.messageList as message>
		  <ul style="font-style: italic;">
		    <li>${message}</li>
		  </ul>
	  </#list>
	  <span style="font-style: italic;">Retrying ...</span>
  </#if>

</body>
</html>