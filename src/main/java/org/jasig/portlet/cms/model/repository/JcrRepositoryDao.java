/*
Licensed to Jasig under one or more contributor license
agreements. See the NOTICE file distributed with this work
for additional information regarding copyright ownership.
Jasig licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file
except in compliance with the License. You may obtain a
copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
 **/
package org.jasig.portlet.cms.model.repository;

import java.io.IOException;
import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.cms.model.Post;
import org.jasig.portlet.cms.model.RepositorySearchOptions;
import org.jcrom.Jcrom;
import org.springmodules.jcr.JcrCallback;
import org.springmodules.jcr.support.JcrDaoSupport;

public class JcrRepositoryDao extends JcrDaoSupport implements RepositoryDao {
	private Jcrom jcrom = null;

	@SuppressWarnings("unused")
	private final Log logger = LogFactory.getLog(getClass());

	@Override
	public Post getPost(final String nodeName) throws JcrRepositoryException {
		final Object post = getTemplate().execute(new JcrCallback() {
			@Override
			public Object doInJcr(final Session session) throws IOException, RepositoryException {
				Post post = null;
				try {
					final PostDao dao = getPostDao(session);

					if (dao.exists(nodeName)) {
						post = dao.get(nodeName);
						post.setPath(nodeName);
					}

				} catch (final RepositoryException e) {
					throw new JcrRepositoryException(e);
				}
				return post;
			}
		});

		if (post != null)
			return (Post) post;
		return null;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Post> search(final RepositorySearchOptions options) throws JcrRepositoryException {
		final Object list = getTemplate().execute(new JcrCallback() {
			@Override
			public Object doInJcr(final Session session) throws IOException, RepositoryException {
				Collection<Post> list = null;
				try {
					final PostDao dao = getPostDao(session);
					list = dao.findAll(options);
				} catch (final RepositoryException e) {
					throw new JcrRepositoryException(e);
				}
				return list;
			}
		});

		if (list != null)
			return (Collection<Post>) list;
		return null;

	}

	public void setJcrom(final Jcrom jcrom) {
		this.jcrom = jcrom;
	}

	@Override
	public void setPost(final Post post) throws JcrRepositoryException {
		getTemplate().execute(new JcrCallback() {
			@Override
			public Object doInJcr(final Session session) throws IOException, RepositoryException {
				try {
					final PostDao dao = getPostDao(session);

					if (dao.exists(post.getPath()))
						dao.update(post);
					else
						dao.create(getSession().getRootNode().getPath(), post);

				} catch (final RepositoryException e) {
					throw new JcrRepositoryException(e);
				}
				return null;
			}
		});
	}

	private Jcrom getJcrom() {
		return jcrom;
	}

	private PostDao getPostDao(final Session session) throws RepositoryException {
		return new PostDao(session, getJcrom());
	}

}