package com.versionone.hudson;

import com.versionone.integration.ciCommon.VcsModification;
import hudson.scm.ChangeLogSet;

import java.util.Date;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: rozhnev
 * Date: 22.10.2009
 * Time: 15:19:10
 * To change this template use File | Settings | File Templates.
 */
public class VcsChanges implements Iterable<VcsModification> {
    private final ChangeLogSet changeSet;

    public VcsChanges(ChangeLogSet changeSet) {
        this.changeSet = changeSet;
    }

    public Iterator<VcsModification> iterator() {
        return new VcsIterator(changeSet.getItems());
    }

    private class VcsIterator implements Iterator<VcsModification>, VcsModification {
        private int i = 0;
        final Object[] items;

        public VcsIterator(Object[] items) {
            this.items = items;
        }

        public boolean hasNext() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public VcsModification next() {
            i++;
            return this;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        //==========================================================
        public String getUserName() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getComment() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Date getDate() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getId() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
