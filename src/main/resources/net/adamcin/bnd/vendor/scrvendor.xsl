<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:param name="vendor_name"/>
    <xsl:param name="root_ns" select="namespace-uri(/descendant-or-self::node()[local-name(current()) = 'component'])"/>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="node()[local-name(current()) = 'component']">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
            <xsl:element name="property" namespace="{$root_ns}">
                <xsl:attribute name="name">service.vendor</xsl:attribute>
                <xsl:attribute name="value"><xsl:value-of select="$vendor_name"/></xsl:attribute>
            </xsl:element>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>