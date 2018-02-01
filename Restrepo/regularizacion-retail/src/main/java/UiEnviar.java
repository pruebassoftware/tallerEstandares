import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.channel.NACChannel;
import org.jpos.iso.packager.ISO87BPackager;

class UiEnviar extends JFrame {
    private JTextField autorizacionJT, cajeroJT, claveJT, fechaJT, horaJT, ipJT, midJT, montoJT, puertoJT, referenciaJT, simCardJt, tidJT;
    private JTextArea visorJTA;

    private UiEnviar() {
        super("REGULARIZACIONES DIRECTV RETAIL V100");
        initComponents();
    }

    private ISOMsg obtenerMensajeISO() {
        ISOMsg request = new ISOMsg("0200");

        try {
            request.set(2, "827629");
            request.set(3, "370000");
            // Monto
            if (montoJT.getText().compareTo("") != 0) {
                request.set(4, montoJT.getText());
            } else {
                return null;
            }
            // MID
            if (midJT.getText().compareTo("") != 0) {
                request.set(42, midJT.getText());
            } else {
                return null;
            }
            // TID|
            if (tidJT.getText().compareTo("") != 0) {
                request.set(41, tidJT.getText());
            } else {
                return null;
            }
            // SIM CARD
            if (simCardJt.getText().compareTo("") != 0) {
                String tempo;
                tempo = ISOUtil.padright(simCardJt.getText(), 20, ' ');
                request.set(112, "1202" + tempo);
            } else {
                return null;
            }
            // referencia
            if (referenciaJT.getText().compareTo("") != 0) {
                String tempo;
                tempo = ISOUtil.padright(referenciaJT.getText(), 6, '0');
                request.set(11, tempo);
            } else {
                return null;
            }
            // autorizacion
            if (autorizacionJT.getText().compareTo("") != 0) {
                String tempo;
                tempo = ISOUtil.padright(autorizacionJT.getText(), 6, '0');
                request.set(38, tempo);
            }
            // Cajero y Clave
            if (cajeroJT.getText().compareTo("") != 0 && claveJT.getText().compareTo("") != 0) {
                String tempo;
                tempo = cajeroJT.getText() + claveJT.getText();
                request.set(116, tempo);
            } else {
                return null;
            }

            // Fecha
            if (fechaJT.getText().compareTo("") != 0) {

                request.set(13, fechaJT.getText());
            } else {
                return null;
            }
            // Hora
            if (horaJT.getText().compareTo("") != 0) {

                request.set(12, horaJT.getText());
            } else {
                return null;
            }

            request.set(24, "026");
            request.set(25, "00");
            request.set(22, "012");

        } catch (Exception ex) {
            Logger.getLogger(UiEnviar.class.getName()).log(Level.SEVERE, null, ex);
        }

        return request;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        JLabel jLabel11 = new JLabel();
        JLabel jLabel12 = new JLabel();
        JLabel jLabel13 = new JLabel();
        JLabel jLabel1 = new JLabel();
        JLabel jLabel2 = new JLabel();
        JLabel jLabel3 = new JLabel();
        JLabel jLabel4 = new JLabel();
        JLabel jLabel5 = new JLabel();
        JLabel jLabel6 = new JLabel();
        JLabel jLabel7 = new JLabel();
        JLabel jLabel8 = new JLabel();
        JLabel jLabel9 = new JLabel();
        JLabel jLabel10 = new JLabel();
        JScrollPane jScrollPane1 = new JScrollPane();
        visorJTA = new JTextArea();
        JButton enviarJB = new JButton();
        JButton salirJB = new JButton();
        tidJT = new JTextField();
        midJT = new JTextField();
        simCardJt = new JTextField();
        referenciaJT = new JTextField();
        autorizacionJT = new JTextField();
        cajeroJT = new JTextField();
        claveJT = new JTextField();
        fechaJT = new JTextField();
        horaJT = new JTextField();
        montoJT = new JTextField();
        ipJT = new JTextField();
        puertoJT = new JTextField();
        JButton borrar = new JButton();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("COMERCIO ID");
        jLabel2.setText("TERMINAL ID");
        jLabel3.setText("SIM CARD");
        jLabel4.setText("REFERENCIA");
        jLabel5.setText("AUTORIZACION");
        jLabel6.setText("CAJERO");
        jLabel7.setText("CLAVE");
        jLabel8.setText("FECHA");
        jLabel9.setText("HORA");
        jLabel10.setFont(new Font("Tahoma", Font.BOLD, 18)); // NOI18N
        jLabel10.setText("REGULARIZAR TRANSACCIONES DIRECTV RETAIL");
        visorJTA.setColumns(20);
        visorJTA.setRows(5);
        jScrollPane1.setViewportView(visorJTA);
        enviarJB.setText("Enviar");
        enviarJB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enviarJBActionPerformed();
            }
        });

        jLabel11.setText("MONTO");
        jLabel12.setText("IP / HOST");
        ipJT.setText("192.168.3.32");
        jLabel13.setText("PUERTO");
        puertoJT.setText("9001");

        borrar.setText("Borrar");
        borrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                BorrarActionPerformed();
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout
                .createSequentialGroup().addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel6)
                                .addComponent(jLabel5, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4).addComponent(jLabel3)
                                .addGroup(layout.createSequentialGroup().addGap(4, 4, 4)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel11).addComponent(jLabel9)))
                                .addComponent(jLabel1).addComponent(jLabel2, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel8).addComponent(jLabel7)))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup().addComponent(jLabel10).addContainerGap(130, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup().addGroup(layout
                                .createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(midJT, GroupLayout.Alignment.TRAILING)
                                .addComponent(tidJT, GroupLayout.Alignment.TRAILING)
                                .addComponent(simCardJt, GroupLayout.Alignment.TRAILING)
                                .addComponent(referenciaJT, GroupLayout.Alignment.TRAILING)
                                .addComponent(autorizacionJT, GroupLayout.Alignment.TRAILING)
                                .addComponent(cajeroJT, GroupLayout.Alignment.TRAILING)
                                .addComponent(claveJT, GroupLayout.Alignment.TRAILING)
                                .addComponent(fechaJT, GroupLayout.Alignment.TRAILING).addComponent(horaJT)
                                .addComponent(montoJT, GroupLayout.Alignment.TRAILING,
                                        GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup().addGap(55, 55, 55)
                                                .addGroup(layout
                                                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel12).addComponent(jLabel13))
                                                .addGap(48, 48, 48)
                                                .addGroup(layout
                                                        .createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(ipJT, GroupLayout.PREFERRED_SIZE, 129,
                                                                GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(puertoJT, GroupLayout.PREFERRED_SIZE,
                                                                62, GroupLayout.PREFERRED_SIZE))
                                                .addGap(25, 25, 25)
                                                .addGroup(layout
                                                        .createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                        .addComponent(enviarJB, GroupLayout.PREFERRED_SIZE,
                                                                72, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(salirJB, GroupLayout.PREFERRED_SIZE,
                                                                75, GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(layout.createSequentialGroup().addGap(18, 18, 18).addComponent(
                                                jScrollPane1, GroupLayout.PREFERRED_SIZE, 401,
                                                GroupLayout.PREFERRED_SIZE)))
                                .addGap(46, 46, 46))))
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(borrar,
                                GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
                        .addGap(213, 213, 213)));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout
                .createSequentialGroup().addGap(23, 23, 23).addComponent(jLabel10).addGap(47, 47, 47)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup().addGroup(layout
                                .createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(jLabel1)
                                .addComponent(midJT, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel12)
                                .addComponent(ipJT, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel2).addComponent(tidJT,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE))
                                        .addGroup(GroupLayout.Alignment.TRAILING,
                                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel13).addComponent(puertoJT,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3).addComponent(simCardJt,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(referenciaJT, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel5)
                                        .addComponent(autorizacionJT, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel6).addComponent(cajeroJT,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(claveJT, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel7))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel8)
                                        .addComponent(fechaJT, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel9).addComponent(horaJT,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel11).addComponent(montoJT,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)))
                        .addGroup(layout.createSequentialGroup().addComponent(enviarJB).addGap(18, 18, 18)
                                .addComponent(salirJB).addGap(18, 18, 18).addComponent(jScrollPane1)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(borrar).addContainerGap()));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void enviarJBActionPerformed() {// GEN-FIRST:event_enviarJBActionPerformed
        ISOMsg requestMsg;
        ISOMsg responseMsg;
        NACChannel channel;
        String ip;
        int puerto;
        byte[] tpdu = {0x60, 0x50, 0x00, 0x00, 0x01};

        requestMsg = obtenerMensajeISO();

        ip = ipJT.getText();
        puerto = Integer.parseInt(puertoJT.getText());

        if (requestMsg != null) {
            channel = new NACChannel(ip, puerto, new ISO87BPackager(), tpdu);
            try {
                channel.setTimeout(15000);
                channel.connect();
                visorJTA.append("CONECTADO A " + ip + ": " + puerto + "\n");
                channel.send(requestMsg);
                visorJTA.append("ENVIANDA PAQUETE ISO8583" + "\n");

                visualizarTramaISO8583(requestMsg, 0);

                responseMsg = channel.receive();
                if (responseMsg != null) {
                    visualizarTramaISO8583(responseMsg, 1);

                } else {
                    visorJTA.append("NO HAY RESPUESTA" + "\n");
                }

            } catch (Exception ex) {
                Logger.getLogger(UiEnviar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void BorrarActionPerformed() {
        visorJTA.setText("");
    }

    private void visualizarTramaISO8583(ISOMsg msg, int flag) {
        int campos;

        if (msg != null) {
            if (flag == 0) {
                visorJTA.append("MSG ISO8583 OUTPUT" + "\n");
            } else {
                visorJTA.append("MSG ISO8583 INPUT" + "\n");
            }

            for (campos = 0; campos <= 128; campos++) {
                if (msg.hasField(campos)) {
                    visorJTA.append("Campo = " + campos + "  Valor = " + msg.getString(campos) + "\n");
                }
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UiEnviar().setVisible(true);
            }
        });
    }
}