<jsp:root xmlns:c="http://java.sun.com/jsp/jstl/core" xmlns:fn="http://java.sun.com/jsp/jstl/functions" xmlns:spring="http://www.springframework.org/tags" xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0">
  <jsp:output omit-xml-declaration="yes" />

  <jsp:directive.attribute name="locale" type="java.lang.String" required="true" rtexprvalue="true" description="The locale for the language to be added." />
  <jsp:directive.attribute name="target" type="java.lang.String" required="true" rtexprvalue="true" description="The target utl." />
  <jsp:directive.attribute name="label" type="java.lang.String" required="true" rtexprvalue="true" description="The country label for the language to be added." />
  <jsp:directive.attribute name="render" type="java.lang.Boolean" required="false" rtexprvalue="true" description="Indicate if the contents of this tag and all enclosed tags should be rendered (default 'true')" />

  <c:if test="${empty render or render}">
    <spring:url var="img" value="/resources/images/${locale}.png" />
    <spring:url var="url" value="${target}">
      <c:if test="${null ne param.form}">
        <spring:param name="form" />
      </c:if>
      <c:if test="${not empty param.find}">
        <spring:param name="find" value="${param.find}" />
      </c:if>
      <spring:param name="lang" value="${locale}" />
      <c:if test="${not empty param.page}">
        <spring:param name="page" value="${param.page}" />
      </c:if>
      <c:if test="${not empty param.size}">
        <spring:param name="size" value="${param.size}" />
      </c:if>
    </spring:url>    
    <spring:message code="global.language.switch" arguments="${label}" var="lang_label" htmlEscape="false" />
    <a href="${url}">
      <img class="flag" src="${fn:escapeXml(img)}" alt="${locale}" />
    </a>
    <c:out value=" " />
  </c:if>
</jsp:root>
