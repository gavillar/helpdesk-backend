package br.com.helpdesk.services;


import br.com.helpdesk.domain.Pessoa;
import br.com.helpdesk.domain.Tecnico;
import br.com.helpdesk.domain.dtos.TecnicoDTO;
import br.com.helpdesk.repositories.PessoaRepository;
import br.com.helpdesk.repositories.TenicoRepository;
import br.com.helpdesk.services.exceptions.DataIntegrityViolationException;
import br.com.helpdesk.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TecnicoService {

    @Autowired
    private TenicoRepository repository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    public Tecnico findById(Integer id) {
        Optional<Tecnico> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException("Objeto Não Encontrado! " + id));
    }

    public List<Tecnico> findAll() {
        return repository.findAll();
    }

    public Tecnico create(TecnicoDTO objDTO) {
        objDTO.setId(null);
        objDTO.setPassword(encoder.encode(objDTO.getPassword()));
        validaAsCpfAndEmail(objDTO);
        Tecnico newObj = new Tecnico(objDTO);
        return repository.save(newObj);
    }

    public Tecnico update(Integer id, TecnicoDTO objDTO) {
        objDTO.setId(id);
        Tecnico oldObj = findById(id);
        validaAsCpfAndEmail(objDTO);
        oldObj = new Tecnico(objDTO);
        return repository.save(oldObj);
    }


    public void delete(Integer id) {
        Tecnico obj = findById(id);
        if( obj.getChamados().size() > 0) {
            throw new DataIntegrityViolationException("Tecnico possui ordem de serviço e não pode ser deletado!");
        }
        repository.deleteById(id);
    }

    private void validaAsCpfAndEmail(TecnicoDTO objDTO) {
        Optional<Pessoa> obj = pessoaRepository.findByCpf(objDTO.getCpf());
        if(obj.isPresent() && obj.get().getId() != objDTO.getId()) {
            throw new DataIntegrityViolationException("CPF já cadastrado no sistema");
        }

        obj = pessoaRepository.findByEmail(objDTO.getEmail());

        if(obj.isPresent() && obj.get().getId() != objDTO.getId()) {
            throw new DataIntegrityViolationException("Email já cadastrado no sistema");
        }
    }



}
