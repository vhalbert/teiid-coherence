/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.teiid.translator.coherence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.teiid.language.Select;
import org.teiid.metadata.BaseColumn.NullType;
import org.teiid.metadata.Datatype;
import org.teiid.metadata.MetadataFactory;
import org.teiid.metadata.Table;
import org.teiid.query.metadata.SystemMetadata;
import org.teiid.translator.ExecutionContext;
import org.teiid.translator.object.ObjectConnection;
import org.teiid.translator.object.ObjectExecution;
import org.teiid.translator.object.ObjectExecutionFactory;
import org.teiid.translator.object.testdata.trades.Trade;
import org.teiid.translator.object.testdata.trades.TradesCacheSource;
import org.teiid.translator.object.testdata.trades.VDBUtility;

@SuppressWarnings("nls")
public class TestCoherenceExecutionFactory  {
	protected static ObjectConnection conn = TradesCacheSource.createConnection();
	
	@Mock
	protected ExecutionContext context;
	
	@Mock
	protected Select command;
	
	protected ObjectExecutionFactory factory;


	public class TestFactory extends CoherenceExecutionFactory {
		public TestFactory() {
			
		}

	}
	

	@Before public void beforeEach() throws Exception{	
 
		MockitoAnnotations.initMocks(this);
		
		factory = createFactory();
    }

	protected ObjectExecutionFactory createFactory() {
		CoherenceExecutionFactory f = new TestFactory();
		
		return f;
	}
	
	@Test public void testFactory() throws Exception {
		factory.start();
			
		ObjectExecution exec = (ObjectExecution) factory.createExecution(command, context, VDBUtility.RUNTIME_METADATA, conn);
		
		assertNotNull(exec);
		assertTrue(factory.getSearchType() instanceof CoherenceSearch);
	}	
	
	
	@Test public void testGetMetadata() throws Exception {
		factory.start();
		
		Map<String, Datatype> dts = SystemMetadata.getInstance().getSystemStore().getDatatypes();

		MetadataFactory mfactory = new MetadataFactory("TestVDB", 1, "Trade",  dts, new Properties(), null);
		
		factory.getMetadata(mfactory, conn);
		
		assertEquals(mfactory.getSchema().getName(), "Trade");
		
		String clzName = Trade.class.getName();
		clzName = clzName.substring(clzName.lastIndexOf(".") + 1);

		Table physicalTable = mfactory.getSchema().getTable(clzName);
		assertNotNull(physicalTable);
		assertTrue(physicalTable.isPhysical());
		assertTrue(!physicalTable.isVirtual());
		assertEquals(5, physicalTable.getColumns().size());
		//this
		assertEquals("object", physicalTable.getColumns().get(0).getRuntimeType());
		//trade id key
		assertEquals("long", physicalTable.getColumns().get(1).getRuntimeType());
		assertEquals(NullType.No_Nulls, physicalTable.getColumns().get(1).getNullType());
		//name
		assertEquals("string", physicalTable.getColumns().get(2).getRuntimeType());
		
		assertEquals(1, physicalTable.getAllKeys().size());
	}	


}
