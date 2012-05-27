/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */

package com.akiban.qp.operator.memoryadapter;

import com.akiban.ais.model.GroupTable;
import com.akiban.ais.model.Index;
import com.akiban.qp.expression.IndexKeyRange;
import com.akiban.qp.operator.Cursor;
import com.akiban.qp.operator.GroupCursor;
import com.akiban.qp.operator.IndexScanSelector;
import com.akiban.qp.operator.QueryContext;
import com.akiban.qp.operator.StoreAdapter;
import com.akiban.qp.operator.API.Ordering;
import com.akiban.qp.operator.API.SortOption;
import com.akiban.qp.row.HKey;
import com.akiban.qp.row.Row;
import com.akiban.qp.rowtype.RowType;
import com.akiban.qp.rowtype.Schema;
import com.akiban.server.service.config.ConfigurationService;
import com.akiban.server.service.session.Session;
import com.akiban.util.tap.InOutTap;

public class MemoryAdapter extends StoreAdapter {

    public MemoryAdapter(Schema schema, 
            Session session,
            ConfigurationService config) {
        super(schema, session, config);
    }

    @Override
    public GroupCursor newGroupCursor(GroupTable groupTable) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HKey newHKey(com.akiban.ais.model.HKey hKeyMetadata) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cursor newIndexCursor(QueryContext context, Index index,
            IndexKeyRange keyRange, Ordering ordering,
            IndexScanSelector scanSelector) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public long getQueryTimeoutSec() {
        return getConfig().queryTimeoutSec();
    }

    @Override
    public long rowCount(RowType tableType) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Cursor sort(QueryContext context, Cursor input, RowType rowType,
            Ordering ordering, SortOption sortOption, InOutTap loadTap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRow(Row oldRow, Row newRow) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void writeRow(Row newRow) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRow(Row oldRow) {
        throw new UnsupportedOperationException();
    }
}