//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.01.17 at 05:13:21 PM EST 
//


package com.kerneldc.metarbatch.xml.schema.metar;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="sky_cover" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="cloud_base_ft_agl" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "sky_condition")
public class SkyCondition extends AbstractBaseBean {

	private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "sky_cover")
    protected String skyCover;
    @XmlAttribute(name = "cloud_base_ft_agl")
    protected Integer cloudBaseFtAgl;

    /**
     * Gets the value of the skyCover property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSkyCover() {
        return skyCover;
    }

    /**
     * Sets the value of the skyCover property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSkyCover(String value) {
        this.skyCover = value;
    }

    /**
     * Gets the value of the cloudBaseFtAgl property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCloudBaseFtAgl() {
        return cloudBaseFtAgl;
    }

    /**
     * Sets the value of the cloudBaseFtAgl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCloudBaseFtAgl(Integer value) {
        this.cloudBaseFtAgl = value;
    }

}
