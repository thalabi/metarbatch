<html>
<head>
  <title>Metar job failure</title>
</head>
<body>

	<#if (jobParametersMap?size gt 0)>
		<div style="font-style: underline;">Job parameters:</div>
		<ul>
			<#list jobParametersMap as name, value>
			<#if (name == jobTimestampFieldName)>
				<li>${name} = ${value?number?number_to_datetime}</li>
			<#else>
				<li>${name} = ${value}</li>
			</#if>
			</#list>  
		</ul>
	</#if>
  
	<#if (stacktraceList?size gt 0)>
    	<div style="font-style: underline;">Exception(s):</div>
		<ul style="font-style: italic;">
			<#list stacktraceList as stacktrace>
		   	<li>${stacktrace}</li>
			</#list>
		</ul>
	</#if>

</body>
</html>