package com.erickgx.api_produtos.controller;


import com.erickgx.api_produtos.models.Produto;
import com.erickgx.api_produtos.repository.ProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/produto") //definição da rota que esse controller escutará
@Slf4j //habilita o controlle de logs para monitoramento
public class ProdutoController {


    //Injeção via Construtor (private final) é a mais recomendada em projetos modernos do Spring Boot,(Li na Doc oficial)
    private final ProdutoRepository repository;

    public ProdutoController(ProdutoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Object> saveProduto(@RequestBody Produto produto){
        try {
            repository.save(produto);
            log.info("Produto criado com sucesso: {}", produto.getId());
            return ResponseEntity.created(URI.create("/produto/"+produto.getId()))
                    .body(produto);
        }catch (Exception e){
            log.error("Erro ao salvar produto: {}", e.getMessage(), e); //log de erro severo para depuração
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping
    //usando List para melhor controle de resposta para a aplicação front end
    //diferentes codigos para lista vazia e lista com dados
    public ResponseEntity<List<Produto>> listarProdutos() {
        List<Produto> produtos = repository.findAll();

        if (produtos.isEmpty()) {
            log.warn("Nenhum produto encontrado."); // Log de aviso
            return ResponseEntity.noContent().build(); // Retorna 204 No Content
        }
        log.info("Listando {} produtos.", produtos.size()); // Log de sucesso
        return ResponseEntity.ok(produtos);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorID(@PathVariable("id") Long id){
        return repository.findById(id)
                //.map() é útil quando queremos transformar um valor, caso ele exista(Optionals), muito util em Get id's.
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (repository.existsById(id)) {  // Verifica se o ID existe antes de tentar deletar
            repository.deleteById(id);
            log.info("Produto deletado com id: {}", id); //log de sucesso
            return ResponseEntity.noContent().build(); // Retorna 204 No Content quando a exclusão é bem-sucedida
        } else {
            log.warn("Tentativa de deletar produto com ID inexistente: {}", id);  // Log de aviso
            return ResponseEntity.notFound().build(); // Retorna 404 caso o registro não exista
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizarProduto(@RequestBody Produto produto,  @PathVariable("id") Long id){
        return repository.findById(id)
                .map(produtoExistente -> {
                    produto.setId(id);
                    Produto atualizado = repository.save(produto);
                    log.info("Produto atualizado com sucesso: {}", id);
                    return ResponseEntity.ok(atualizado);
                })
                .orElseGet(() ->{
                    log.warn("Tentativa de atualizar produto inexistente: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }





}
