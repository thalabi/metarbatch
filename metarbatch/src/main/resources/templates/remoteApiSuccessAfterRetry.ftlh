<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
</head>
<body>

     <span style="font-style: underline;">${httpRequestTypeEnum.serviceDescription} succeeded after ${retryCount} retries</span>

	<#if systemMessage??>
		<div><strong style="color: red;">Email quota reached. This is the last email of the day. Check logs for further messages.</strong></div>
	</#if>

</body>
</html>