/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

import modelo.ConsultaDAO;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Map;

/**
 * VentanaPrincipal - La Vista en el patrón MVC Usa ConsultaDAO para obtener
 * datos de la base de datos.
 *
 * @author Marcelo-HP
 */
public class VentanaPrincipal extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(VentanaPrincipal.class.getName());

    /**
     * Creates new form VentanaPrincipal
     */
    public VentanaPrincipal() {
        // Configurar UTF-8 para toda la aplicación
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("client.encoding.override", "UTF-8");

        initComponents();
        cargarTablasEnComboBox();
        agregarPestanaScripts();
    }

    /**
     * Carga las tablas de la base de datos en el ComboBox
     */
    private void cargarTablasEnComboBox() {
        try {
            ConsultaDAO dao = new ConsultaDAO();
            List<String> tablas = dao.obtenerNombresDeTablas();

            DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>();
            modelo.addElement("-- Seleccione una tabla --");

            for (String nombreTabla : tablas) {
                modelo.addElement(nombreTabla);
            }

            jComboBoxNombreTabla.setModel(modelo);
            logger.info("Tablas cargadas exitosamente en el ComboBox");

        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Error al cargar tablas", e);
            JOptionPane.showMessageDialog(this,
                    "Error al cargar las tablas: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra los datos de una tabla en el JTable
     */
    private void mostrarDatosEnTabla(String nombreTabla) {
        try {
            ConsultaDAO dao = new ConsultaDAO();
            List<String> columnas = dao.obtenerColumnasDeTabla(nombreTabla);
            List<Map<String, Object>> datos = dao.obtenerDatosDeTabla(nombreTabla);

            DefaultTableModel modeloTabla = new DefaultTableModel();

            for (String columna : columnas) {
                modeloTabla.addColumn(columna);
            }

            for (Map<String, Object> fila : datos) {
                Object[] filaArray = new Object[columnas.size()];
                for (int i = 0; i < columnas.size(); i++) {
                    filaArray[i] = fila.get(columnas.get(i));
                }
                modeloTabla.addRow(filaArray);
            }

            jTable1.setModel(modeloTabla);
            jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);

            logger.info("Datos de " + nombreTabla + " mostrados. Registros: " + datos.size());

            if (datos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "La tabla está vacía",
                        "Información",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Error al mostrar datos", e);
            JOptionPane.showMessageDialog(this,
                    "Error al mostrar datos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Agrega una nueva pestaña con botones para ejecutar scripts PL/SQL
     */
    private void agregarPestanaScripts() {
        javax.swing.JPanel panelScripts = new javax.swing.JPanel();
        panelScripts.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 10));

        // Botón para ejecutar izrael.sql
        javax.swing.JButton btnIzrael = new javax.swing.JButton("Ejecutar Reporte Multas Atraso");
        btnIzrael.addActionListener(e -> ejecutarScript("REPORTE_MULTAS_ATRASO.sql"));
        panelScripts.add(btnIzrael);

        // Botón para ejecutar consulta.sql
        //javax.swing.JButton btnConsulta = new javax.swing.JButton("Ejecutar SQLMarcelo");
        //btnConsulta.addActionListener(e -> ejecutarScript("historial_pagos.sql"));
        //panelScripts.add(btnConsulta);
        // Botón para ejecutar Ingrid_Nunez.sql
        //javax.swing.JButton btnIngrid = new javax.swing.JButton("Ejecutar sql Ingrid_Nuñez");
        //btnIngrid.addActionListener(e -> ejecutarScript("reporte_multas_atraso.sql"));
        //panelScripts.add(btnIngrid);
        // Botón para ejecutar todos
        javax.swing.JButton btnTodos = new javax.swing.JButton("⚡ Ejecutar TODOS");
        btnTodos.setBackground(new java.awt.Color(255, 153, 51));
        btnTodos.setForeground(java.awt.Color.WHITE);
        btnTodos.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        btnTodos.addActionListener(e -> ejecutarTodosLosScripts());
        panelScripts.add(btnTodos);

        // Agregar instrucciones
        javax.swing.JTextArea txtInstrucciones = new javax.swing.JTextArea(
                "\n📋 INSTRUCCIONES:\n\n"
                + "• Haz clic en un botón para ejecutar el script correspondiente\n"
                + "• Los scripts crearán y llenarán tablas automáticamente\n"
                + "• Después de ejecutar, ve a la pestaña 'Listar' para ver los datos\n\n"
                + "Scripts disponibles:\n"
                + "  - reporte_multas_atraso.sql → REPORTE_MULTAS_ATRASO\n");
        txtInstrucciones.setEditable(false);
        txtInstrucciones.setBackground(new java.awt.Color(245, 245, 245));
        txtInstrucciones.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 11));

        javax.swing.JScrollPane scrollInstrucciones = new javax.swing.JScrollPane(txtInstrucciones);
        scrollInstrucciones.setPreferredSize(new java.awt.Dimension(800, 250));

        javax.swing.JPanel panelContenedor = new javax.swing.JPanel();
        panelContenedor.setLayout(new java.awt.BorderLayout(10, 10));
        panelContenedor.add(panelScripts, java.awt.BorderLayout.NORTH);
        panelContenedor.add(scrollInstrucciones, java.awt.BorderLayout.CENTER);

        jTabbedPaneConsultas.addTab("Scripts PL/SQL", panelContenedor);
    }

    /**
     * Ejecuta un script SQL específico
     */
    private void ejecutarScript(String nombreArchivo) {
        ConsultaDAO dao = new ConsultaDAO();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 EJECUTANDO: " + nombreArchivo);
        System.out.println("=".repeat(60));

        // Ejecutar y obtener los mensajes de salida
        List<String> mensajes = dao.ejecutarBloquePLSQLConSalida(nombreArchivo);

        if (mensajes.isEmpty() || mensajes.get(0).startsWith("ERROR")) {
            // Mostrar error
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("✗ Error al ejecutar el script\n\n");
            for (String mensaje : mensajes) {
                errorMsg.append(mensaje).append("\n");
            }

            JOptionPane.showMessageDialog(this,
                    errorMsg.toString(),
                    "Error - " + nombreArchivo,
                    JOptionPane.ERROR_MESSAGE);
        } else {
            // Mostrar éxito con los mensajes de DBMS_OUTPUT
            StringBuilder successMsg = new StringBuilder();
            successMsg.append("✓ Script ejecutado correctamente\n\n");
            successMsg.append("Resultados:\n");
            successMsg.append("─".repeat(40)).append("\n");

            for (String mensaje : mensajes) {
                if (!mensaje.startsWith("✓")) {
                    successMsg.append("• ").append(mensaje).append("\n");
                }
            }

            JOptionPane.showMessageDialog(this,
                    successMsg.toString(),
                    "Éxito - " + nombreArchivo,
                    JOptionPane.INFORMATION_MESSAGE);

            // Recargar las tablas en el ComboBox
            cargarTablasEnComboBox();
        }
    }

    /**
     * Ejecuta todos los scripts SQL en orden
     */
    private void ejecutarTodosLosScripts() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que quieres ejecutar TODOS los scripts?\n\n"
                + "Se ejecutarán:\n"
                + "  • izrael.sql\n"
                + "  • consulta.sql\n"
                + "  • Ingrid_Nunez.sql\n\n"
                + "Esto puede tomar unos segundos.",
                "Confirmar ejecución",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (respuesta == JOptionPane.YES_OPTION) {
            ConsultaDAO dao = new ConsultaDAO();
            dao.ejecutarTodosLosScripts();

            JOptionPane.showMessageDialog(this,
                    "✓ Todos los scripts fueron ejecutados\n\n"
                    + "Revisa la consola para ver los resultados detallados\n"
                    + "Ve a la pestaña 'Listar' para ver los datos",
                    "Ejecución completada",
                    JOptionPane.INFORMATION_MESSAGE);

            // Recargar las tablas en el ComboBox
            cargarTablasEnComboBox();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPaneConsultas = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jButtonListar = new javax.swing.JButton();
        jComboBoxNombreTabla = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButtonListar.setText("Listar");
        jButtonListar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonListarActionPerformed(evt);
            }
        });

        jComboBoxNombreTabla.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxNombreTabla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxNombreTablaActionPerformed(evt);
            }
        });

        jScrollPane2.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(51, 51, 51)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 811,
                                                Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jButtonListar)
                                                .addGap(18, 18, 18)
                                                .addComponent(jComboBoxNombreTabla,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 247,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap()));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButtonListar)
                                        .addComponent(jComboBoxNombreTabla, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        jTabbedPaneConsultas.addTab("Listar", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPaneConsultas));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPaneConsultas));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxNombreTablaActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jComboBoxNombreTablaActionPerformed
        // TODO add your handling code here
        String tabla = (String) jComboBoxNombreTabla.getSelectedItem();
        if (tabla != null && !tabla.startsWith("--")) {
            System.out.println("Seleccionaste: " + tabla);
            // Aquí puedes listar los datos de esa tabla
        }

    }// GEN-LAST:event_jComboBoxNombreTablaActionPerformed

    private void jButtonListarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonListarActionPerformed
        String tablaSeleccionada = (String) jComboBoxNombreTabla.getSelectedItem();

        if (tablaSeleccionada == null || tablaSeleccionada.startsWith("--")) {
            JOptionPane.showMessageDialog(this,
                    "Por favor selecciona una tabla primero",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Mostrar los datos en el JTable con el formato correcto
        mostrarDatosEnTabla(tablaSeleccionada);
    }// GEN-LAST:event_jButtonListarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonListar;
    private javax.swing.JComboBox<String> jComboBoxNombreTabla;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPaneConsultas;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
