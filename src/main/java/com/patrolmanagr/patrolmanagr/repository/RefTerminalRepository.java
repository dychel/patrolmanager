package com.patrolmanagr.patrolmanagr.repository;

import com.patrolmanagr.patrolmanagr.entity.Ref_site;
import com.patrolmanagr.patrolmanagr.entity.Ref_terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.Ref;

@Repository
public interface RefTerminalRepository extends JpaRepository<Ref_terminal, Long> {

    @Query("select ref_terminal from Ref_terminal ref_terminal where ref_terminal.id = :id")
    Ref_terminal findByIdTerminal(@Param("id") Long id);

    @Query("select ref_terminal from Ref_terminal ref_terminal where ref_terminal.ref_vendor_id.id = :id")
    Ref_terminal findByIdVendor(@Param("id") Long id);

    @Query("select ref_terminal from Ref_terminal ref_terminal where ref_terminal = :terminal_type")
    Ref_terminal findByTerminalType(@Param("terminal_type") String terminal_type);

    @Query("select ref_terminal from Ref_terminal ref_terminal where ref_terminal.code = :code")
    Ref_terminal findByCode(@Param("code") String code);

    @Query("select ref_terminal from Ref_terminal ref_terminal where ref_terminal.ref_site.id = :id")
    Ref_terminal findRef_terminalBySite(@PathVariable("id") Long id);
}
