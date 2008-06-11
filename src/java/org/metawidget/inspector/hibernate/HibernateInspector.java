// Metawidget
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package org.metawidget.inspector.hibernate;

import static org.metawidget.inspector.InspectionResultConstants.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.metawidget.inspector.ConfigReader;
import org.metawidget.inspector.ResourceResolver;
import org.metawidget.inspector.iface.InspectorException;
import org.metawidget.inspector.impl.BaseXmlInspector;
import org.metawidget.util.CollectionUtils;
import org.metawidget.util.XmlUtils;
import org.metawidget.util.simple.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Inspector to look for relevant settings in hibernate.cfg.xml and mapping.hbm.xml files.
 *
 * @author Richard Kennard
 */

public class HibernateInspector
	extends BaseXmlInspector
{
	//
	//
	// Private statics
	//
	//

	private final static String		HIBERNATE_CONFIGURATION_ELEMENT	= "hibernate-configuration";

	private final static String		HIBERNATE_MAPPING_ELEMENT		= "hibernate-mapping";

	private final static String[]	EMPTY_STRING_ARRAY				= new String[0];

	//
	//
	// Private members
	//
	//

	private boolean					mHideIds;

	//
	//
	// Constructor
	//
	//

	public HibernateInspector()
	{
		this( new HibernateInspectorConfig() );
	}

	public HibernateInspector( HibernateInspectorConfig config )
	{
		this( config, new ConfigReader() );
	}

	public HibernateInspector( ResourceResolver resolver )
	{
		this( new HibernateInspectorConfig(), resolver );
	}

	public HibernateInspector( HibernateInspectorConfig config, ResourceResolver resolver )
	{
		super( config, resolver );

		mHideIds = config.isHideIds();
	}

	//
	//
	// Protected methods
	//
	//

	/**
	 * Overriden to search by <code>name=</code>, not <code>type=</code>.
	 */

	@Override
	protected String getTopLevelTypeAttribute()
	{
		return NAME;
	}

	@Override
	protected String getTypeAttribute()
	{
		return "class";
	}

	/**
	 * Hibernate supports <code>extends</code> via its <code>&lt;subclass&gt;</code> element.
	 */
	
	@Override
	protected String getExtendsAttribute()
	{
		return "extends";
	}

	/**
	 * Overriden to automatically drill into Hibernate Configuration files.
	 */

	@Override
	protected Element getDocumentElement( DocumentBuilder builder, ResourceResolver resolver, InputStream... files )
		throws Exception
	{
		Document documentMaster = null;

		for ( InputStream file : files )
		{
			Document documentParsed = builder.parse( file );

			if ( !documentParsed.hasChildNodes() )
				continue;

			// If the document is a hibernate-configuration file...

			Element parsed = documentParsed.getDocumentElement();
			String nodeName = parsed.getNodeName();

			if ( HIBERNATE_CONFIGURATION_ELEMENT.equals( nodeName ) )
			{
				// ...look up each hibernate-mapping file...

				Element sessionFactory = XmlUtils.getChildNamed( documentParsed.getDocumentElement(), "session-factory" );
				Element mapping = XmlUtils.getChildNamed( sessionFactory, "mapping" );

				List<String> fileList = CollectionUtils.newArrayList();

				while( mapping != null )
				{
					fileList.add( mapping.getAttribute( "resource" ) );
					mapping = XmlUtils.getSiblingNamed( mapping, "mapping" );
				}

				// ...and combine them

				parsed = getDocumentElement( builder, resolver, fileList.toArray( EMPTY_STRING_ARRAY ) );

				if ( documentMaster == null || !documentMaster.hasChildNodes() )
				{
					documentMaster = parsed.getOwnerDocument();
					continue;
				}
			}

			// ...otherwise, read hibernate-mapping files

			else if ( HIBERNATE_MAPPING_ELEMENT.equals( nodeName ) )
			{
				preprocessDocument( documentParsed );

				if ( documentMaster == null || !documentMaster.hasChildNodes() )
				{
					documentMaster = documentParsed;
					continue;
				}
			}
			else
			{
				throw InspectorException.newException( "Expected an XML document starting with '" + HIBERNATE_CONFIGURATION_ELEMENT + "' or '" + HIBERNATE_MAPPING_ELEMENT + "', but got '" + nodeName + "'" );
			}

			XmlUtils.combineElements( documentMaster.getDocumentElement(), parsed, getTopLevelTypeAttribute(), getNameAttribute() );
		}

		if ( documentMaster == null )
			return null;

		return documentMaster.getDocumentElement();
	}

	/**
	 * Prepend 'package' attribute to class 'name' and 'extends' attributes, and
	 * to 'class' attributes of children.
	 */

	@Override
	protected void preprocessDocument( Document document )
	{
		Element root = document.getDocumentElement();
		String pkg = root.getAttribute( "package" );

		if ( pkg != null && !"".equals( pkg ) )
		{
			NodeList children = root.getChildNodes();
			String topLevelAttribute = getTopLevelTypeAttribute();
			String extendsAttribute = getExtendsAttribute();

			for ( int loop = 0, length = children.getLength(); loop < length; loop++ )
			{
				Node node = children.item( loop );

				if ( ! ( node instanceof Element ) )
					continue;

				Element element = (Element) node;

				// 'name' attribute of 'class'/'subclass' element

				String name = element.getAttribute( topLevelAttribute );

				if ( name != null && !"".equals( name ) && name.indexOf( StringUtils.SEPARATOR_DOT ) == -1 )
					element.setAttribute( topLevelAttribute, pkg + StringUtils.SEPARATOR_DOT + name );

				// 'extends' attribute of 'subclass' element

				String extendsClass = element.getAttribute( extendsAttribute );

				if ( extendsClass != null && !"".equals( extendsClass ) && extendsClass.indexOf( StringUtils.SEPARATOR_DOT ) == -1 )
					element.setAttribute( extendsAttribute, pkg + StringUtils.SEPARATOR_DOT + extendsClass );

				// 'class' attributes of children

				prependPackageToClassAttribute( element, pkg );
			}
		}
	}

	@Override
	protected Map<String, String> inspect( Element toInspect )
	{
		Map<String, String> attributes = CollectionUtils.newHashMap();

		// Hibernate has a rich DTD for its mapping files. We try to parse it
		// without explicitly enumerating every possible type of node

		// Name

		attributes.put( NAME, toInspect.getAttribute( NAME ) );

		// Do not just copy 'type': Hibernate types are not the POJO type

		String type = toInspect.getAttribute( TYPE );

		// Large

		if ( "clob".equals( type ) )
		{
			attributes.put( TYPE, String.class.getName() );
			attributes.put( LARGE, TRUE );
		}

		// Class, on the other hand, IS a reliable indicator of POJO type

		String typeAttribute = getTypeAttribute();
		
		if ( toInspect.hasAttribute( typeAttribute ))
			attributes.put( TYPE, toInspect.getAttribute( typeAttribute ));

		// Required

		if ( TRUE.equals( toInspect.getAttribute( "not-null" ) ) )
			attributes.put( REQUIRED, TRUE );

		// Length

		if ( toInspect.hasAttribute( "length" ))
			attributes.put( MAXIMUM_LENGTH, toInspect.getAttribute( "length" ));

		// Hidden

		String nodeName = toInspect.getNodeName();

		if ( mHideIds && "id".equals( nodeName ) )
			attributes.put( HIDDEN, TRUE );

		// Parameterized

		if ( "bag".equals( nodeName ) || "list".equals( nodeName ) || "set".equals( nodeName ) )
		{
			Element withClass = XmlUtils.getChildWithAttribute( toInspect, typeAttribute );

			if ( withClass != null )
				attributes.put( PARAMETERIZED_TYPE, withClass.getAttribute( typeAttribute ) );
		}

		return attributes;
	}

	//
	//
	// Private methods
	//
	//

	private void prependPackageToClassAttribute( Element element, String pkg )
	{
		// For each child...

		NodeList children = element.getChildNodes();

		String typeAttribute = getTypeAttribute();
		
		for ( int loop = 0, length = children.getLength(); loop < length; loop++ )
		{
			Node node = children.item( loop );

			if ( ! ( node instanceof Element ) )
				continue;

			Element child = (Element) node;

			// ...fix 'class' attribute...

			String clazz = child.getAttribute( typeAttribute );

			if ( clazz != null && !"".equals( clazz ) && clazz.indexOf( StringUtils.SEPARATOR_DOT ) == -1 )
				child.setAttribute( typeAttribute, pkg + StringUtils.SEPARATOR_DOT + clazz );

			// ...and recurse children

			prependPackageToClassAttribute( child, pkg );
		}
	}
}
