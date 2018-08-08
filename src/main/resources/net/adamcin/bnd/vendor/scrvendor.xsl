<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <!--
    Component description documents may be embedded in other XML documents.
    SCR will process all XML documents listed in the Service-Component manifest header of a bundle.
    XML documents containing component descriptions may contain a single, root component element or one or more component elements embedded in a
    larger document. Use of the namespace for component descriptions is mandatory.
    The attributes and subelements of a component element are always unqualified.
    -->

    <!-- this is set by VendorPlugin when creating a new Transformer -->
    <xsl:param name="vendor_name"/>

   <!-- identity template -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!--
    The OSGi DS / SCR spec defines different namespaces for different versions of the SCR xml versions.
    We can't simply match against the current namespace. Instead, we find the first element with a local-name of "component"
    and persist its namspace-uri as a parameter. This makes the matching logic less obvious.
    -->
    <xsl:template match="node()[local-name() = 'component'][starts-with(namespace-uri(), 'http://www.osgi.org/xmlns/scr')]">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
            <!-- descendent elements of scr:component are always namespace-unqualified -->
            <xsl:element name="property">
                <xsl:attribute name="name">service.vendor</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="$vendor_name"/></xsl:attribute>
            </xsl:element>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>