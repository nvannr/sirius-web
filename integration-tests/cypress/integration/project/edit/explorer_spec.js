/*******************************************************************************
 * Copyright (c) 2021 Obeo.
 * This program and the accompanying materials
 * are made available under the erms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
describe('/projects/:projectId/edit - Explorer', () => {
  beforeEach(() => {
    cy.deleteAllProjects();
    cy.createProject('Cypress Project').then((res) => {
      const projectId = res.body.data.createProject.project.id;
      const robot_flow_id = 'c26b6086-b444-3ee6-b8cd-9a4fde5956a7';
      cy.createDocument(projectId, robot_flow_id, 'robot').then((res) => {
        cy.visit(`/projects/${projectId}/edit`);
      });
    });
  });

  it('contains the documents', () => {
    cy.getByTestId('explorer').contains('robot');
  });

  it('can expand a tree item', () => {
    cy.getByTestId('robot').dblclick();
    cy.getByTestId('explorer').contains('Robot');

    cy.getByTestId('Robot').dblclick();
    cy.getByTestId('explorer').contains('Central_Unit');
    cy.getByTestId('explorer').contains('CaptureSubSystem');
    cy.getByTestId('explorer').contains('Wifi');
  });

  it('can collapse a tree item', () => {
    cy.getByTestId('robot').dblclick();
    cy.getByTestId('Robot').dblclick();
    cy.getByTestId('robot').dblclick();

    cy.getByTestId('Robot').should('not.exist');
  });

  it('can expand a tree item without changing the selection', () => {
    cy.getByTestId('robot').dblclick();
    cy.getByTestId('selected').contains('robot');
    cy.getByTestId('explorer').contains('Robot');

    cy.getByTestId('Robot-toggle').click();
    cy.getByTestId('explorer').contains('Robot');
    cy.getByTestId('explorer').contains('Central_Unit');
    cy.getByTestId('explorer').contains('CaptureSubSystem');
    cy.getByTestId('explorer').contains('Wifi');
  });

  it('can collapse a tree item without changing the selection', () => {
    // Select and expand 'robot' root
    cy.getByTestId('robot').dblclick();
    cy.getByTestId('selected').contains('robot');
    cy.getByTestId('explorer').contains('Robot');

    // Select and expand 'Robot' child
    cy.getByTestId('Robot').dblclick();
    cy.getByTestId('selected').contains('Robot');
    cy.getByTestId('explorer').contains('Central_Unit');
    cy.getByTestId('explorer').contains('CaptureSubSystem');
    cy.getByTestId('explorer').contains('Wifi');

    // Select the 'robot' root
    cy.getByTestId('robot').click();
    cy.getByTestId('selected').contains('robot');
    cy.getByTestId('Robot').should('exist');
    cy.getByTestId('explorer').contains('Central_Unit');
    cy.getByTestId('explorer').contains('CaptureSubSystem');
    cy.getByTestId('explorer').contains('Wifi');

    // Collapse the 'Robot' child while keeping the selection on 'robot' root
    cy.getByTestId('Robot-toggle').click();
    cy.getByTestId('selected').contains('robot');
    cy.getByTestId('Robot').should('exist');
    cy.getByTestId('explorer').contains('Central_Unit').should('not.exist');
    cy.getByTestId('explorer').contains('CaptureSubSystem').should('not.exist');
    cy.getByTestId('explorer').contains('Wifi').should('not.exist');
  });

  it('can select an object', () => {
    cy.getByTestId('robot').dblclick();
    cy.getByTestId('Robot').dblclick();
    cy.getByTestId('Wifi').click();

    cy.getByTestId('selected').contains('Wifi');
  });

  it('can select a document', () => {
    cy.getByTestId('robot').click();
    cy.getByTestId('selected').contains('robot');
  });

  it('can rename a selected document with simple click (direct edit)', () => {
    cy.getByTestId('robot').click();
    cy.getByTestId('selected').contains('robot');
    cy.getByTestId('robot').click();
    cy.getByTestId('robot').type('renamed-robot{enter}');
    cy.getByTestId('renamed-robot').should('exist');
  });

  it('can rename a selected document by start typing (direct edit)', () => {
    cy.getByTestId('robot').click();
    cy.getByTestId('selected').contains('robot');
    cy.getByTestId('robot').type('renamed-robot{enter}');
    cy.getByTestId('renamed-robot').should('exist');
  });

  it('can cancel a direct edit with Escape', () => {
    cy.getByTestId('robot').click();
    cy.getByTestId('selected').contains('robot');
    cy.getByTestId('robot').click();
    cy.getByTestId('robot').type('renamed-robot{esc}');
    cy.getByTestId('robot').should('exist');
  });

  it('can apply a direct edit with focus lost', () => {
    cy.getByTestId('robot').dblclick();
    cy.getByTestId('Robot').should('exist');
    cy.getByTestId('robot').click();
    cy.getByTestId('robot').type('renamed-robot');
    cy.getByTestId('Robot').click();
    cy.getByTestId('renamed-robot').should('exist');
    // While this solution is bad, it allows React to re-render and OCP to reselect the newly edited element
    // A proper solution would involve waiting for the focus to change after the focus lost but that seems way more complex
    cy.wait(1000);
    cy.getByTestId('selected').contains('renamed-robot');
  });
});
