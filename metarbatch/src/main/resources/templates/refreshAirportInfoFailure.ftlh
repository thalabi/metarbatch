<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
</head>
<body>

  <#if (loadingFromExternalApiException.messageList?size gt 0)>
     <span style="font-style: underline;">Refresh airport information failed with the following exceptions:</span>
	  <#list loadingFromExternalApiException.messageList as message>
		  <ul style="font-style: italic;">
		    <li>${message}</li>
		  </ul>
	  </#list>
  </#if>

</body>
</html>