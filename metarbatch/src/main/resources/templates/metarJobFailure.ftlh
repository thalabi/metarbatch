<html>
<head>
  <title>Metar job failure</title>
</head>
<body>

	<#if (jobParametersMap?size gt 0)>
		<div style="font-style: underline;">Job parameters:</div>
		<ul>
			<#list jobParametersMap as key, jobParameter>
				<#if (jobParameter.type?contains("Date"))>
					<li>${key} = ${jobParameter.value?string("yyyy-MM-dd HH:mm:ss")}</li>
				<#else>
					<li>${key} = ${jobParameter.value}</li>
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