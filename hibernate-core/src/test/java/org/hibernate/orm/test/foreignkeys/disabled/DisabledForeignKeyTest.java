/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.foreignkeys.disabled;

import java.util.EnumSet;
import java.util.Map;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Table.ForeignKeyKey;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Steve Ebersole
 */
public class DisabledForeignKeyTest extends BaseUnitTestCase {

	@Test
	@TestForIssue( jiraKey = "HHH-9704" )
	public void basicTests() {
		StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
		StandardServiceRegistry standardRegistry = registryBuilder.build();
		try {
			final MetadataSources sources = new MetadataSources( standardRegistry );

			sources.addAnnotatedClass( ManyToManyOwner.class );
			sources.addAnnotatedClass( ManyToManyTarget.class );

			final MetadataImplementor metadata = (MetadataImplementor) sources.buildMetadata();
			metadata.orderColumns( false );
			metadata.validate();

			new SchemaExport().execute(
					EnumSet.of( TargetType.STDOUT ),
					SchemaExport.Action.CREATE,
					metadata
			);

			int fkCount = 0;
			for ( Table table : metadata.collectTableMappings() ) {
				for ( Map.Entry<ForeignKeyKey, ForeignKey> entry : table.getForeignKeys().entrySet() ) {
					assertFalse(
							"Creation for ForeignKey [" + entry.getKey() + "] was not disabled",
							entry.getValue().isCreationEnabled()
					);
					fkCount++;
				}
			}

			// ultimately I want to actually create the ForeignKet reference, but simply disable its creation
			// via ForeignKet#disableCreation()
			assertEquals( "Was expecting 4 FKs", 0, fkCount );
		}
		finally {
			StandardServiceRegistryBuilder.destroy( standardRegistry );
		}
	}

	@Test
	@TestForIssue( jiraKey = "HHH-9704" )
	public void expandedTests() {
		StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
		StandardServiceRegistry standardRegistry = registryBuilder.build();
		try {
			final MetadataSources sources = new MetadataSources( standardRegistry );

			sources.addAnnotatedClass( ManyToManyOwner.class );
			sources.addAnnotatedClass( ManyToManyTarget.class );

			final MetadataImplementor metadata = (MetadataImplementor) sources.buildMetadata();
			metadata.orderColumns( false );
			metadata.validate();

			// export the schema
			new SchemaExport().execute(
					EnumSet.of( TargetType.DATABASE ),
					SchemaExport.Action.BOTH,
					metadata
			);

			try {
				// update the schema
				new SchemaUpdate().execute( EnumSet.of( TargetType.DATABASE ), metadata );
			}
			finally {
				// drop the schema
				new SchemaExport().execute(
						EnumSet.of( TargetType.DATABASE ),
						SchemaExport.Action.DROP,
						metadata
				);
			}
		}
		finally {
			StandardServiceRegistryBuilder.destroy( standardRegistry );
		}
	}

}
