package com.adobe.aem.guides.wknd.core.servlets;

import com.adobe.aem.guides.wknd.core.models.BlogModel;
import com.adobe.aem.guides.wknd.core.services.BlogService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.*;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import java.io.IOException;
import java.io.PrintWriter;


@Component(
        service = {Servlet.class},
        property = {
                "sling.servlet.paths=/bin/blog",
                "sling.servlet.methods=POST,PUT,GET,DELETE"
        }
)
public class BlogServlet extends SlingAllMethodsServlet {

    @Reference
    private BlogService blogService;

    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        ResourceResolver resolver = request.getResourceResolver();

        BlogModel blogPost = new BlogModel();

        blogPost.setTitle(request.getParameter("title"));
        blogPost.setSubtitle(request.getParameter("subtitle"));
        blogPost.setImagePath(request.getParameter("imagePath"));
        blogPost.setContent(request.getParameter("content"));
        blogPost.setAuthor(request.getParameter("author"));
        blogPost.setPublicationDate(request.getParameter("publicationDate"));
        blogPost.setTags(request.getParameter("tags"));

        try {
            blogService.createPost(resolver, blogPost);
            response.setStatus(SlingHttpServletResponse.SC_CREATED);
            response.getWriter().write("Post criado com sucesso!");
        } catch (IllegalArgumentException e) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Erro: " + e.getMessage());
        } catch (RepositoryException e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Erro ao salvar o blog: " + e.getMessage());
        }

        writer.close();
    }


    protected void doPut(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ResourceResolver resolver = request.getResourceResolver();
        PrintWriter writer = response.getWriter();

        String oldTitle = request.getParameter("oldTitle");
        String title = request.getParameter("title");

        try {
            // Busca o recurso atual pelo título antigo
            Resource blogResource = resolver.getResource("/content/blog/" + oldTitle);

            if (blogResource == null) {
                response.setStatus(SlingHttpServletResponse.SC_NOT_FOUND);
                writer.write("Post não encontrado.");
                return;
            }

            // Adapta o recurso para BlogModel
            BlogModel blogPost = blogResource.adaptTo(BlogModel.class);
            if (blogPost == null) {
                response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.write("Erro ao adaptar o recurso para BlogModel.");
                return;
            }

            // Se o título foi alterado, renomeia o nó
            if (!oldTitle.equals(title)) {
                Session session = resolver.adaptTo(Session.class);
                if (session != null) {
                    String parentPath = blogResource.getParent().getPath();
                    String newPath = parentPath + "/" + title;
                    session.move(blogResource.getPath(), newPath);
                    session.save();

                    // Atualiza a referência do recurso após mover
                    blogResource = resolver.getResource(newPath);
                    blogPost = blogResource.adaptTo(BlogModel.class);
                }
            }


            //Verificando os dados se são nulos ou iguais aqueles que já estao definidos no post antes de sobrescrever com os novos valores do post
            if (title != null && !title.equals(blogPost.getTitle())) {
                blogPost.setTitle(title);
            }

            String subtitle = request.getParameter("subtitle");
            if (subtitle != null && !subtitle.equals(blogPost.getSubtitle())) {
                blogPost.setSubtitle(subtitle);
            }

            String imagePath = request.getParameter("imagePath");
            if (imagePath != null && !imagePath.equals(blogPost.getImagePath())) {
                blogPost.setImagePath(imagePath);
            }

            String content = request.getParameter("content");
            if (content != null && !content.equals(blogPost.getContent())) {
                blogPost.setContent(content);
            }

            String author = request.getParameter("author");
            if (author != null && !author.equals(blogPost.getAuthor())) {
                blogPost.setAuthor(author);
            }

            String publicationDate = request.getParameter("publicationDate");
            if (publicationDate != null && !publicationDate.equals(blogPost.getPublicationDate())) {
                blogPost.setPublicationDate(publicationDate);
            }

            String tags = request.getParameter("tags");
            if (tags != null && !tags.equals(blogPost.getTags())) {
                blogPost.setTags(tags);
            }

            blogService.updatePost(resolver, blogPost);
            response.setStatus(SlingHttpServletResponse.SC_OK);
            writer.write("Blog atualizado com sucesso!");
        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.write("Erro ao atualizar o blog: " + e.getMessage());
        } finally {
            writer.close();
        }
    }

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();

        try {
            String tag = request.getParameter("tag");
            String title = request.getParameter("title");

            if (tag != null && !tag.isEmpty()) { // Buscar posts por tag
                JSONArray blogs = blogService.getPostsByTag(request.getResourceResolver(), tag);
                writer.write(blogs.toString());

            } else if (title != null && !title.isEmpty()) { // Busca por título
                JSONObject blogPost = blogService.getPostByName(request.getResourceResolver(), title);

                if (blogPost != null) {
                    writer.write(blogPost.toString());
                } else {
                    response.setStatus(SlingHttpServletResponse.SC_NOT_FOUND);
                    writer.write("Post não encontrado.");
                }

            } else { // Caso contrário retorna todos os posts
                JSONArray blogs = blogService.getAllPosts(request.getResourceResolver());
                writer.write(blogs.toString());
            }
        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            writer.write("Erro: " + e.getMessage());
        } finally {
            writer.close();
        }
    }


    protected void doDelete(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();
        ResourceResolver resolver = request.getResourceResolver();

        try {
            blogService.deletePost(resolver, request.getParameter("title"));
            writer.write("Post do blog deletado com sucesso!");
            response.setStatus(SlingHttpServletResponse.SC_OK);
        } catch (PersistenceException e) {
            writer.write("Erro ao tentar deletar blog: " + e.getMessage());
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            writer.close();
        }
    }
}
