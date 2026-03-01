<html>
<head>
  <title>Created Metar partition</title>
</head>
<body>

	Created new Metar table partition for year <strong>${year?string("0000")}</strong> and month <strong>${month?string("00")}</strong>
	 
	<#if systemMessage??>
		<div><strong style="color: red;">Email quota reached. This is the last email of the day. Check logs for further messages.</strong></div>
	</#if>
</body>
</html>