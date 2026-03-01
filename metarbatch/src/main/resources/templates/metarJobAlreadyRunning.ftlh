<html>
<head>
  <title>Metar job already running</title>
</head>
<body>

	<#if (currentJobParametersMap?size gt 0)>
		<div style="font-style: underline;">Job parameters:</div>
		<ul>
			<#list currentJobParametersMap as name, value>
			<#if (name == jobTimestampFieldName)>
				<li>${name} = ${value?number?number_to_datetime}</li>
			<#else>
				<li>${name} = ${value}</li>
			</#if>
			</#list>  
		</ul>
	</#if>
  
	<#if (runningJobParametersMap?size gt 0)>
		<div style="font-style: underline;">Running job parameters:</div>
		<ul>
			<#list runningJobParametersMap as name, value>
			<#if (name == jobTimestampFieldName)>
				<li>${name} = ${value?number?number_to_datetime}</li>
			<#else>
				<li>${name} = ${value}</li>
			</#if>
			</#list>  
		</ul>
	</#if>
  
	<#if systemMessage??>
		<div><strong style="color: red;">Email quota reached. This is the last email of the day. Check logs for further messages.</strong></div>
	</#if>

</body>
</html>