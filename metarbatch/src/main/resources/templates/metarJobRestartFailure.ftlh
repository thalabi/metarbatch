<html>
<head>
  <title>Metar job restart failure</title>
</head>
<body>

		<div style="font-style: underline;">Job execution id: ${jobExecutionId}</div>
  
    	<div style="font-style: underline;">Exception:</div>
    	<ul style="font-style: italic;">
		   	<li>${stacktrace}</li>
		</ul>

	<#if systemMessage??>
		<div><strong style="color: red;">Email quota reached. This is the last email of the day. Check logs for further messages.</strong></div>
	</#if>

</body>
</html>