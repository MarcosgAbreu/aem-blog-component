package com.adobe.aem.guides.wknd.core.services;

import com.adobe.aem.guides.wknd.core.models.BlogModel;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

@Component(service = BlogService.class)
public class BlogService {

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public void deletePost(ResourceResolver resolver, String nodeTitle) throws PersistenceException {
        Resource blogPost = searchNode(resolver, nodeTitle);
        resolver.delete(blogPost);
        resolver.commit();
    }

    public void createPost(ResourceResolver resolver, BlogModel blogPost) throws RepositoryException, PersistenceException {

        // Validação de campos em branco
        if (!blogPost.isValid()) {
            throw new IllegalArgumentException("Dados inválidos para o blog");
        }

        // Verificar se o post com o mesmo título já existe
        Resource existingPost = resolver.getResource("/content/blog/" + blogPost.getTitle());
        if (existingPost != null) {
            throw new IllegalArgumentException("Um post com esse título já existe.");
        }

        // Cria nó blog no JCR caso não exista
        Resource blogRoot = resolver.getResource("/content/blog");
        if (blogRoot == null) {
            blogRoot = resolver.create(resolver.getResource("/content"), "blog", null);
        }

        String formattedDate = sdf.format(blogPost.getPublicationDate());

        // Criar um novo recurso para o blog e salvar os dados do BlogModel no JCR
        Resource newPost = resolver.create(blogRoot, blogPost.getTitle(), null);
        ModifiableValueMap properties = newPost.adaptTo(ModifiableValueMap.class);
        properties.put("title", blogPost.getTitle());
        properties.put("subtitle", blogPost.getSubtitle());
        properties.put("imagePath", blogPost.getImagePath());
        properties.put("content", blogPost.getContent());
        properties.put("author", blogPost.getAuthor());
        properties.put("publicationDate", convertDate(formattedDate));
        properties.put("tags", blogPost.getTags());

        resolver.commit();
    }

    public void updatePost(ResourceResolver resolver, BlogModel blogPost) throws RepositoryException, PersistenceException {

        // Validação de campos em branco
        if (!blogPost.isValid()) {
            throw new IllegalArgumentException("Dados inválidos para o post. Há compos em branco");
        }

        String formattedDate = sdf.format(blogPost.getPublicationDate());
        Resource blogResource = resolver.getResource("/content/blog/" + blogPost.getTitle());

        if (blogResource != null) {
            // Atualizando as propriedades do recurso existente
            ModifiableValueMap properties = blogResource.adaptTo(ModifiableValueMap.class);
            if (properties != null) {
                properties.put("title", blogPost.getTitle());
                properties.put("subtitle", blogPost.getSubtitle());
                properties.put("imagePath", blogPost.getImagePath());
                properties.put("content", blogPost.getContent());
                properties.put("author", blogPost.getAuthor());
                properties.put("publicationDate", convertDate(formattedDate));
                properties.put("tags", blogPost.getTags());
            }

            resolver.commit();
        }
    }

    // Buscar todos os posts
    public JSONArray getAllPosts(ResourceResolver resolver) throws Exception {
        Resource blogResource = resolver.getResource("/content/blog");

        return convertToJSONArray(blogResource);
    }

    // Buscar post por nome
    public JSONObject getPostByName(ResourceResolver resolver, String postTitle) throws PersistenceException {
        Resource blogResource = searchNode(resolver, postTitle);

        return convertResourceToJSON(blogResource);
    }

    public JSONArray getPostsByTag(ResourceResolver resolver, String tag) throws Exception {
        JSONArray blogsArray = new JSONArray();

        // Acessa a raiz onde os posts estão armazenados
        Resource blogResource = resolver.getResource("/content/blog");

        if (blogResource != null) {
            // Itera sobre todos os posts
            for (Resource blog : blogResource.getChildren()) {
                // Obtém a string de tags do recurso
                String tagsString = blog.getValueMap().get("tags", String.class);

                if (tagsString != null) {
                    // Divide a string de tags em um array
                    String[] tags = tagsString.split(",");

                    // Verifica se a tag fornecida está na lista de tags
                    if (arrayContains(tags, tag)) {
                        // Adiciona o post à resposta se a tag estiver presente
                        JSONObject blogObject = convertResourceToJSON(blog);
                        blogsArray.put(blogObject);
                    }
                }
            }
        }
        return blogsArray;
    }


    //MÉTODOS AUXILIÁRES
    private JSONObject convertResourceToJSON(Resource resource) {
        String title = resource.getValueMap().get("title", String.class);
        String content = resource.getValueMap().get("content", String.class);
        String author = resource.getValueMap().get("author", String.class);
        String publicationDate = resource.getValueMap().get("publicationDate", String.class);
        String imagePath = resource.getValueMap().get("imagePath", String.class);
        String tags = resource.getValueMap().get("tags", String.class);

        JSONObject blogObject = new JSONObject();
        blogObject.put("title", title);
        blogObject.put("content", content);
        blogObject.put("author", author);
        blogObject.put("publicationDate", publicationDate);
        blogObject.put("imagePath", imagePath);
        blogObject.put("tags", tags);

        return blogObject;
    }
    // Método auxiliar para verificar se uma array contém uma string específica
    private boolean arrayContains(String[] array, String value) {
        for (String element : array) {
            if (element.trim().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    // Método auxiliar para converter um Resource em JSON
    private JSONArray convertToJSONArray(Resource blogResource) {
        JSONArray blogsArray = new JSONArray();

        for (Resource blog : blogResource.getChildren()) {
            String title = blog.getValueMap().get("title", String.class);
            String content = blog.getValueMap().get("content", String.class);
            String author = blog.getValueMap().get("author", String.class);
            String publicationDate = blog.getValueMap().get("publicationDate", String.class);
            String imagePath = blog.getValueMap().get("imagePath", String.class);
            String tags = blog.getValueMap().get("tags", String.class);

            JSONObject blogObject = new JSONObject();
            blogObject.put("title", title);
            blogObject.put("content", content);
            blogObject.put("author", author);
            blogObject.put("publicationDate", publicationDate);
            blogObject.put("imagePath", imagePath);
            blogObject.put("tags", tags);

            blogsArray.put(blogObject);
        }
        return blogsArray;
    }


    public Calendar convertDate(String dateString) {

        try {
            // Converte a string de data para um objeto Calendar
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(sdf.parse(dateString));

            return calendar;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public Resource searchNode(ResourceResolver resolver, String nodeTitle) throws PersistenceException {
        Resource blogResource = resolver.getResource("/content/blog/" + nodeTitle);
        if (blogResource == null) {
            throw new PersistenceException("Post do blog não encotrado.");
        }
        return blogResource;
    }
}
