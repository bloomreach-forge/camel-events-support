<#include "../include/imports.ftl">

<@hst.setBundle basename="essentials.pagenotfound"/>
<div>
  <h1><@fmt.message key="pagenotfound.title" var="title"/>${title?html}</h1>
  <p><@fmt.message key="pagenotfound.text"/><#--Skip XML escaping--></p>
</div>
<@hst.include ref="container"/>